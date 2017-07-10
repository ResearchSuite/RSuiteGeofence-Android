package edu.cornell.tech.foundry.geofence;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.util.Log;

import net.sqlcipher.database.SQLiteDatabase;

import org.researchstack.backbone.ResourcePathManager;
import org.researchstack.backbone.StorageAccess;
import org.researchstack.backbone.storage.database.AppDatabase;
import org.researchstack.backbone.storage.database.sqlite.SqlCipherDatabaseHelper;
import org.researchstack.backbone.storage.database.sqlite.UpdatablePassphraseProvider;
import org.researchstack.backbone.storage.file.UnencryptedProvider;
import org.researchstack.skin.DataResponse;

import java.io.UnsupportedEncodingException;

import edu.cornell.tech.foundry.ohmageomhsdk.OhmageOMHManager;
import edu.cornell.tech.foundry.researchsuitetaskbuilder.RSTBStateHelper;
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

    public void initializeGeofenceManager(){
        RSTBStateHelper stateHelper = RSGeofenceTaskBuilderManager.getBuilder().getStepBuilderHelper().getStateHelper();

        double homeLat = 0;
        double homeLng = 0;
        double workLat = 0;
        double workLng = 0;

        byte[] homeLatByte = stateHelper.valueInState(this,"latitude_home");
        byte[] homeLngByte = stateHelper.valueInState(this,"longitude_home");
        byte[] workLatByte = stateHelper.valueInState(this,"latitude_work");
        byte[] workLngByte = stateHelper.valueInState(this,"longitude_work");

        if(homeLatByte != null && homeLngByte != null && workLatByte != null && workLngByte != null){
            try {
                String homeLatString = new String(homeLatByte, "UTF-8");
                String homeLngString = new String(homeLngByte, "UTF-8");
                String workLatString = new String(workLatByte, "UTF-8");
                String workLngString = new String(workLngByte, "UTF-8");

                homeLat = Double.parseDouble(homeLatString);
                homeLng = Double.parseDouble(homeLngString);
                workLat = Double.parseDouble(workLatString);
                workLng = Double.parseDouble(workLngString);

                RSuiteGeofenceManager.init(this,homeLat,homeLng,workLat,workLng);



            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        else {
            RSuiteGeofenceManager.init(this,homeLat,homeLng,workLat,workLng);
        }






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

                        Log.d("testing order: ","signed out");
                        RSGeofenceFileAccess.getInstance().clearFileAccess(RSGeofenceApplication.this);


                        if (e != null) {
                            Log.d("testing order: ","not signed out");
                            singleSubscriber.onError(e);
                        }
                        else {
                            Log.d("testing order: ","signed out");
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
