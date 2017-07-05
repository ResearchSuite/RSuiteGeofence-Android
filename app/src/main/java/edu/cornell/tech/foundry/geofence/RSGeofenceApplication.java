package edu.cornell.tech.foundry.geofence;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import net.sqlcipher.database.SQLiteDatabase;

import org.researchstack.backbone.ResourcePathManager;
import org.researchstack.backbone.StorageAccess;
import org.researchstack.backbone.storage.database.AppDatabase;
import org.researchstack.backbone.storage.database.sqlite.SqlCipherDatabaseHelper;
import org.researchstack.backbone.storage.database.sqlite.UpdatablePassphraseProvider;
import org.researchstack.backbone.storage.file.UnencryptedProvider;
import org.researchstack.skin.DataResponse;

import edu.cornell.tech.foundry.ohmageomhsdk.OhmageOMHManager;
import rx.Single;
import rx.SingleSubscriber;

/**
 * Created by jameskizer on 4/12/17.
 */

public class RSGeofenceApplication extends Application {

    @Override
    public void onCreate()
    {
        super.onCreate();
        this.initializeSingletons(this);
    }

    public void initializeSingletons(Context context) {

        //TODO: Change to pin encrypted
        UnencryptedProvider encryptionProvider = new UnencryptedProvider();

        AppDatabase dbAccess = createAppDatabaseImplementation(context);
        dbAccess.setEncryptionKey(encryptionProvider.getEncrypter().getDbKey());

        RSGeofenceFileAccess fileAccess = createFileAccessImplementation(context);
        fileAccess.setEncrypter(encryptionProvider.getEncrypter());

        StorageAccess.getInstance().init(
                null,
                new UnencryptedProvider(),
                fileAccess,
                createAppDatabaseImplementation(context)
        );

        String directory = context.getApplicationInfo().dataDir;


        OhmageOMHManager.config(
                context,
                getString(R.string.omh_base_url),
                getString(R.string.omh_client_id),
                getString(R.string.omh_client_secret),
                fileAccess,
                getString(R.string.ohmage_queue_directory)
        );

        RSGeofenceResourcePathManager resourcePathManager = new RSGeofenceResourcePathManager();
        ResourcePathManager.init(resourcePathManager);
        //config task builder singleton
        //task builder requires ResourceManager, ImpulsivityAppStateManager
        RSGeofenceTaskBuilderManager.init(context, resourcePathManager, fileAccess);

        //config results processor singleton
        //requires RSRPBackend
//        RSGeofenceResultsProcessorManager.init(ORBEOhmageResultBackEnd.getInstance());
//        RSRPResultsProcessor resultsProcessor = new RSRPResultsProcessor(ORBEOhmageResultBackEnd.getInstance());

    }

    public void resetSingletons(Context context) {



        this.initializeSingletons(context);

    }



    protected RSGeofenceFileAccess createFileAccessImplementation(Context context)
    {
        String pathName = "/yadl";
        return new RSGeofenceFileAccess(pathName);
    }

    protected AppDatabase createAppDatabaseImplementation(Context context) {
        SQLiteDatabase.loadLibs(context);

        return new SqlCipherDatabaseHelper(
                context,
                SqlCipherDatabaseHelper.DEFAULT_NAME,
                null,
                SqlCipherDatabaseHelper.DEFAULT_VERSION,
                new UpdatablePassphraseProvider()
        );
    }

    public Single<DataResponse> signOut(Context context) {

        return Single.create(new Single.OnSubscribe<DataResponse>() {
            @Override
            public void call(final SingleSubscriber<? super DataResponse> singleSubscriber) {
                OhmageOMHManager.getInstance().signOut(new OhmageOMHManager.Completion() {
                    @Override
                    public void onCompletion(Exception e) {

                        RSGeofenceFileAccess.getInstance().clearFileAccess(RSGeofenceApplication.this);


                        if (e != null) {
                            singleSubscriber.onError(e);
                        }
                        else {
                            singleSubscriber.onSuccess(new DataResponse(true, "success"));
                        }
                    }
                });
            }
        });

    }

    @Override
    protected void attachBaseContext(Context base)
    {
        // This is needed for android versions < 5.0 or you can extend MultiDexApplication
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

}
