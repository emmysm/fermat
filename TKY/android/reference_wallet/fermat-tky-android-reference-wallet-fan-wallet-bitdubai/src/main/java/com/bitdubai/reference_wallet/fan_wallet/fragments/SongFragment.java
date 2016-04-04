package com.bitdubai.reference_wallet.fan_wallet.fragments;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bitdubai.fermat_android_api.layer.definition.wallet.AbstractFermatFragment;
import com.bitdubai.fermat_api.layer.all_definition.navigation_structure.enums.Wallets;
import com.bitdubai.fermat_pip_api.layer.platform_service.error_manager.enums.UnexpectedWalletExceptionSeverity;
import com.bitdubai.fermat_pip_api.layer.platform_service.error_manager.interfaces.ErrorManager;
import com.bitdubai.fermat_tky_api.all_definitions.enums.ExternalPlatform;
import com.bitdubai.fermat_tky_api.all_definitions.enums.SongStatus;
import com.bitdubai.fermat_tky_api.layer.external_api.interfaces.music.MusicUser;
import com.bitdubai.fermat_tky_api.layer.identity.fan.interfaces.Fan;
import com.bitdubai.fermat_tky_api.layer.song_wallet.exceptions.CantDeleteSongException;
import com.bitdubai.fermat_tky_api.layer.song_wallet.exceptions.CantGetSongListException;
import com.bitdubai.fermat_tky_api.layer.song_wallet.exceptions.CantUpdateSongStatusException;
import com.bitdubai.fermat_tky_api.layer.song_wallet.interfaces.WalletSong;
import com.bitdubai.fermat_tky_api.layer.wallet_module.FanWalletPreferenceSettings;
import com.bitdubai.fermat_tky_api.layer.wallet_module.interfaces.FanWalletModule;
import com.bitdubai.fermat_tky_api.layer.wallet_module.interfaces.FanWalletModuleManager;
import com.bitdubai.reference_wallet.fan_wallet.R;
import com.bitdubai.reference_wallet.fan_wallet.common.adapters.SongAdapter;
import com.bitdubai.reference_wallet.fan_wallet.common.models.SongItems;
import com.bitdubai.reference_wallet.fan_wallet.session.FanWalletSession;
import com.bitdubai.reference_wallet.fan_wallet.util.ManageRecyclerviewClick;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Created by Miguel Payarez on 16/03/16.
 */
public class SongFragment extends AbstractFermatFragment {
    //FermatManager
    private FanWalletSession fanwalletSession;
    private FanWalletModuleManager fanwalletmoduleManager;
    private FanWalletPreferenceSettings fanWalletSettings;
    private ErrorManager errorManager;
    private FanWalletModule fanWalletModule;


    RecyclerView recyclerView;
    SwipeRefreshLayout swipeContainer;
    String TAG="SONGFRAGMENT";
    View view;
    private Paint p = new Paint();
    DownloadThreadClass downloadthread;
    SyncThreadClass syncthread;
    MonitorThreadClass monitorThreadClass;
    private SongAdapter adapter;
    private RecyclerView.LayoutManager lManager;


    List<SongItems> items=new ArrayList();
    public static SongFragment newInstance(FanWalletSession fanwalletSession,
                                           FanWalletModuleManager fanwalletmoduleManager,
                                           ErrorManager errorManager){

        return new SongFragment(fanwalletSession,
                fanwalletmoduleManager,
                errorManager);
    }

    public SongFragment(){

    }
    @SuppressLint("ValidFragment")
    public SongFragment(FanWalletSession fanwalletSession,
                        FanWalletModuleManager fanwalletmoduleManager,
                        ErrorManager errorManager){

        this.fanwalletmoduleManager=fanwalletmoduleManager;
        this.fanwalletSession=fanwalletSession;
        this.errorManager=errorManager;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view= inflater.inflate(R.layout.tky_fan_wallet_song_fragment,container,false);
        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        swipeContainer.setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW);
        recyclerView = (RecyclerView) view.findViewById(R.id.rv);
        lManager = new LinearLayoutManager(view.getContext());
        recyclerView.setLayoutManager(lManager);
        initvalues();
        adapter = new SongAdapter(items);
        recyclerView.setAdapter(adapter);


        swipe_effect();


        getActivity().getWindow().setBackgroundDrawableResource(R.drawable.fanwallet_background_viewpager);

        recyclerView.addOnItemTouchListener(
                new ManageRecyclerviewClick(view.getContext(), new ManageRecyclerviewClick.OnItemClickListener() {
                    @Override

                    public void onItemClick(View view, int position) {

                        System.out.println("click position:"+position);
                        if (items.get(position).getStatus().equals(SongStatus.AVA.getCode())) {
                            askquestion(position, null, 1);
                        } else if (items.get(position).getStatus().equals(SongStatus.DED.getCode())) {
                            playsong();
                        }else if(items.get(position).getStatus().equals(SongStatus.DNG.getCode())){
                            askquestion(position,null,2);
                        }
                    }
                })
        );

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //just for Demo
                /*(new Handler()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeContainer.setRefreshing(false);

                    }
                }, 1500);*/
                syncTokenlyAndUpdateThreads(false);

                //
                // updatesonglist();

            }
        });



    return view;
    }


    void syncTokenlyAndUpdateThreads(boolean autosync){

        syncthread=new SyncThreadClass(autosync); // Firstthread
        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) // Above Api Level 13
        {
            syncthread.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        else // Below Api Level 13
        {
            syncthread.execute();
        }
        monitorThreadClass=new MonitorThreadClass(); // Secondthread
        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)// Above Api Level 13
        {
            monitorThreadClass.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        else // Below Api Level 13
        {
            monitorThreadClass.execute();
        }
    }


    void initvalues(){
        fanWalletModule=fanwalletmoduleManager.getFanWalletModule();
        syncTokenlyAndUpdateThreads(true);
        compareViewAndDatabase();

    }


    void compareViewAndDatabase(){
        String databaseInfo;
        List<String> listComposerAndSongNameOnView=new ArrayList<>();
        List<WalletSong> songsInDatabase=new ArrayList<>();

        try {
            songsInDatabase=fanWalletModule.getAvailableSongs();
        } catch (CantGetSongListException e) {
            e.printStackTrace();
        }

        for(SongItems songitems : items){
            if(!listComposerAndSongNameOnView.contains(songitems.getArtist_name()+"@#@#"+songitems.getSong_name())){
             //   System.out.println("TKY_VIEW songs"+songitems.getArtist_name()+"@#@#"+songitems.getSong_name());
                listComposerAndSongNameOnView.add(songitems.getArtist_name()+"@#@#"+songitems.getSong_name());
            }
        }
        if(songsInDatabase.size()>listComposerAndSongNameOnView.size()){
            for (WalletSong walletitems :songsInDatabase){
                databaseInfo=walletitems.getComposers()+"@#@#"+walletitems.getName();
              //  System.out.println("TKY_WALLET songs"+walletitems.getComposers()+"@#@#"+walletitems.getName());
                if(!listComposerAndSongNameOnView.contains(databaseInfo)){
                    listComposerAndSongNameOnView.add("TKY_WALLET songs" + walletitems.getComposers() + "@#@#" + walletitems.getName());
                    //System.out.println("TKY_NOT in view" + walletitems.getComposers() + "@#@#" + walletitems.getName());
                    items.add(new SongItems(R.drawable.tky_tokenly_album, walletitems.getName(), walletitems.getComposers(), SongStatus.DED.getCode(),walletitems.getSongId(), 0, false));
                    adapter.setFilter(items);
                }
            }

        }
    }



    void swipe_effect(){


   //     ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT ) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder source, RecyclerView.ViewHolder target) {
                return true;

            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                int position = viewHolder.getAdapterPosition();
                List<SongItems> original=new ArrayList<>();
                original.addAll(items);
                askquestion(position, original, 0);

            }

            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                int position=viewHolder.getAdapterPosition();
                final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                final int swipeFlags = ItemTouchHelper.START;       // delete one in case you want just one direction
                if(items.get(position).getStatus()=="Downloading" ||items.get(position).getStatus()=="Pending"){
                    return 0;
                }else{
                    return makeMovementFlags(dragFlags, swipeFlags);
                }

             //   return  items.get(position).getStatus()=="Downloading"? 0:makeMovementFlags(dragFlags, swipeFlags);
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                Bitmap icon;
                if(actionState == ItemTouchHelper.ACTION_STATE_SWIPE){

                    View itemView = viewHolder.itemView;
                    float height = (float) itemView.getBottom() - (float) itemView.getTop();
                    float width = height / 3;

                    if(dX > 0){
                        p.setColor(Color.parseColor("#388E3C"));
                        RectF background = new RectF((float) itemView.getLeft(), (float) itemView.getTop(), dX,(float) itemView.getBottom());
                        c.drawRect(background,p);
                        icon = BitmapFactory.decodeResource(getResources(), R.drawable.tky_chat);
                        //Start draw left-top to right-bottom     RectF (left,top,right,bottom)
                        RectF icon_dest = new RectF((float) itemView.getLeft() + (width/4) ,(float) itemView.getTop() + 0.75f*width,(float) itemView.getLeft()+ 2f*width,(float)itemView.getBottom() -0.75f*width);
                        c.drawBitmap(icon,null,icon_dest,p);
                    } else {
                        p.setColor(Color.parseColor("#D32F2F"));
                        RectF background = new RectF((float) itemView.getRight() + dX, (float) itemView.getTop(),(float) itemView.getRight(), (float) itemView.getBottom());
                        c.drawRect(background,p);
                        icon = BitmapFactory.decodeResource(getResources(), R.drawable.tky_trash);
                        RectF icon_dest = new RectF((float) itemView.getRight() - 1.75f*width ,(float) itemView.getTop() + 0.75f*width,(float) itemView.getRight() - width/4,(float)itemView.getBottom() - 0.75f*width);
                        c.drawBitmap(icon,null,icon_dest,p);
                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }


        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }


    void askquestion(final int position, final List<SongItems> original, int whocallme){
        final UUID songid;
        if(whocallme==0) {
                AlertDialog.Builder dialogo1 = new AlertDialog.Builder(view.getContext());
                dialogo1.setTitle("FanWallet");
                dialogo1.setMessage("Do you really want to delete '" + items.get(position).getSong_name() + "' from your device?");
                dialogo1.setCancelable(false);
                dialogo1.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogo1, int id) {

                        deletesong(position);

                    }
                });
                dialogo1.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogo1, int id) {
                        adapter.setFilter(original);
                    }
                });
                dialogo1.show();
        }else if(whocallme==1){
            AlertDialog.Builder dialogo1 = new AlertDialog.Builder(view.getContext());
            dialogo1.setTitle("FanWallet");
            dialogo1.setMessage("Do you really want to download '" + items.get(position).getSong_name() + "'?");
            dialogo1.setCancelable(false);
            dialogo1.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogo1, int id) {

                    downloadsong(position);

                }
            });
            dialogo1.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogo1, int id) {
                    //Nothing happen
                }
            });
            dialogo1.show();
        }else if(whocallme==2){
            AlertDialog.Builder dialogo1 = new AlertDialog.Builder(view.getContext());
            dialogo1.setTitle("FanWallet");
            dialogo1.setMessage("Do you really want to Cancel the download of '" + items.get(position).getSong_name() + "'?");
            dialogo1.setCancelable(false);
            dialogo1.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogo1, int id) {

                    cancelsong(position);


                }
            });
            dialogo1.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogo1, int id) {
                    //Nothing happen
                }
            });
            dialogo1.show();
        }
    }

    void deletesong(int position){

        try {
            fanWalletModule.deleteSong(items.get(position).getSong_id());
            items.get(position).setStatus(SongStatus.AVA.getCode());
            items.get(position).setProgress(0);
            adapter.setFilter(items);
        } catch (CantDeleteSongException e) {
            errorManager.reportUnexpectedWalletException(Wallets.TKY_FAN_WALLET, UnexpectedWalletExceptionSeverity.DISABLES_SOME_FUNCTIONALITY_WITHIN_THIS_FRAGMENT, e);
        } catch (CantUpdateSongStatusException e) {
            errorManager.reportUnexpectedWalletException(Wallets.TKY_FAN_WALLET, UnexpectedWalletExceptionSeverity.DISABLES_SOME_FUNCTIONALITY_WITHIN_THIS_FRAGMENT, e);
        }

    }

    void downloadsong(int position){
        downloadthread = new DownloadThreadClass(position);
        downloadthread.execute();
    }

    void cancelsong(int position){
        try {

            downloadthread.cancel(true);

        } catch (Exception e) {
            Log.v(TAG, "EXCEPTION"+e);
        }

    }

    @Override
    public void onUpdateViewOnUIThread(String code) {
        super.onUpdateViewOnUIThread(code);
        if (code.split("")[0].equals("1")) {

        }else if(code.split("")[0].equals("1")){

        }else if(code.split("")[0].equals("1")){

        }
    }

    void playsong(){

    }


    /* AsyncTask
    Variable type
    Void for the parameters
    Float for the onprogressupdate
    Boolen for the  onPostExecute   */
    public class DownloadThreadClass extends AsyncTask<Void, Float, Boolean> {
    int position;
        /**
         * parameters position
         */
        public DownloadThreadClass(int position) {
            this.position=position;
        }

        /**
         * Before start thread
         */
        @Override
        protected void onPreExecute() {

            items.get(position).setStatus("Downloading");
            items.get(position).setProgressbarvissible(true);
            Log.v(TAG, "Before start download");
            adapter.setFilter(items);

        }

        /**
         * Se ejecuta después de "onPreExecute". Se puede llamar al hilo Principal con el método "publishProgress" que ejecuta el método "onProgressUpdate" en hilo Principal
         */
        @Override
        protected Boolean doInBackground(Void... variableNoUsada) {

            float progreso = 0.0f;

            while(items.get(position).getProgress()<100) {
                    if (isCancelled()) {
                       break;
                    }
                    try {
                        Thread.sleep((long) (1000));
                    } catch (InterruptedException e) {
                   //     cancel(true); //just in case something goes wrong
                   //     e.printStackTrace();
                    }

                    progreso = progreso+15;

                    publishProgress(progreso);


            }

            return true;
        }



        /**
         * To update the view
         */
        @Override
        protected void onProgressUpdate(Float... porcentajeProgreso) {

            Log.v(TAG, "tiempo:" + porcentajeProgreso[0] + "");

            items.get(position).setProgress(Math.round(porcentajeProgreso[0]));

            adapter.setFilter(items);
        }

        String crono(float tiempo){
            float horas,minutos;
            int segundos;
            String conteo="NADA";

            return conteo;
        }

        /**
         * afte finish receive the value of doinbackground
         */

        protected void onPostExecute(Boolean ready) {
            items.get(position).setStatus("Downloaded");
            items.get(position).setProgressbarvissible(false);
            Log.v(TAG, "Game Over thread");
            adapter.setFilter(items);
        }
        /**
         * when the method cancel is called
         */
        @Override
        protected void onCancelled() {
            items.get(position).setStatus("Pending");
            items.get(position).setProgress(0);
            items.get(position).setProgressbarvissible(false);
            Log.v(TAG, "Donwload canceled");
            adapter.setFilter(items);
        }





    }


    /* AsyncTask
   Variable type
   Void for the parameters
   Float for the onprogressupdate
   Boolen for the  onPostExecute   */
    public class SyncThreadClass extends AsyncTask<Void, WalletSong, Boolean> {
        boolean autosync;
        boolean unfinish=true;

        /**
         * parameters position
         */
        public SyncThreadClass(boolean autosync) {
            this.autosync=autosync;
        }

        /**
         * Before start thread
         */
        @Override
        protected void onPreExecute() {

        }

        /**
         * Se ejecuta después de "onPreExecute". Se puede llamar al hilo Principal con el método "publishProgress" que ejecuta el método "onProgressUpdate" en hilo Principal
         */
        @Override
        protected Boolean doInBackground(Void... variableNoUsada) {

            try {
                Fan testfan=getTestFanIdentity();
                if(autosync) {
                    System.out.println("TKY_AutoSync ok");
                    fanwalletmoduleManager.getFanWalletModule().synchronizeSongs(testfan);
                }else{
                    System.out.println("TKY_ManualSync ok");
                    fanwalletmoduleManager.getFanWalletModule().synchronizeSongsByUser(testfan);
                }
            }catch (Exception e ){
                System.out.println("TKY_Error manual sync:" + e);
            }


            return true;
        }



        /**
         * To update the view
         */
        @Override
        protected void onProgressUpdate(WalletSong... walletitems) {


        }





        /**
         * afte finish receive the value of doinbackground
         */

        protected void onPostExecute(Boolean ready) {
            unfinish=false;
            Log.v(TAG, "Game Over SyncThreadClass");

        }
        /**
         * when the method cancel is called
         */
        @Override
        protected void onCancelled() {

        }





    }

    /* AsyncTask
  Variable type
  Void for the parameters
  Float for the onprogressupdate
  Boolen for the  onPostExecute   */
    public class MonitorThreadClass extends AsyncTask<Void, WalletSong, Boolean> {

        boolean unfinish=true;

        List<WalletSong> songlistofthread=new ArrayList<>();
        List<String> listComposerAndSongNameOnView=new ArrayList<>();
        /**
         * parameters position
         */
        public MonitorThreadClass() {

        }

        /**
         * Before start thread
         */
        @Override
        protected void onPreExecute() {

        }

        /**
         * Se ejecuta después de "onPreExecute". Se puede llamar al hilo Principal con el método "publishProgress" que ejecuta el método "onProgressUpdate" en hilo Principal
         */
        @Override
        protected Boolean doInBackground(Void... variableNoUsada) {

            while(syncthread.unfinish){
                try {
                    songlistofthread=fanwalletmoduleManager.getFanWalletModule().getAvailableSongs();
                    compareViewAndDatabase(songlistofthread, items);
           //         System.out.println("TKY_Monitor ok");
                } catch (CantGetSongListException e) {
                    System.out.println("tky_monitorthread:"+e);;
                }
            }


            return true;
        }

        void compareViewAndDatabase(List<WalletSong> listAvailableSongs,List<SongItems> listSongInView ){
            String databaseInfo;

            for(SongItems songitems : listSongInView){
                if(!listComposerAndSongNameOnView.contains(songitems.getArtist_name()+"@#@#"+songitems.getSong_name())){
                    System.out.println("TKY_VIEW songs"+songitems.getArtist_name()+"@#@#"+songitems.getSong_name());
                    listComposerAndSongNameOnView.add(songitems.getArtist_name()+"@#@#"+songitems.getSong_name());
                }
            }
            if(listAvailableSongs.size()>listComposerAndSongNameOnView.size()){
                for (WalletSong walletitems :listAvailableSongs){
                    databaseInfo=walletitems.getComposers()+"@#@#"+walletitems.getName();
                    System.out.println("TKY_WALLET songs"+walletitems.getComposers()+"@#@#"+walletitems.getName());
                    if(!listComposerAndSongNameOnView.contains(databaseInfo)){
                        listComposerAndSongNameOnView.add("TKY_WALLET songs"+walletitems.getComposers()+"@#@#"+walletitems.getName());
                        System.out.println("TKY_NOT in view"+walletitems.getComposers()+"@#@#"+walletitems.getName());
                        publishProgress(walletitems);
                    }
                }

            }
        }

        /**
         * To update the view
         */
        @Override
        protected void onProgressUpdate(WalletSong... walletitems) {
            System.out.println("TKY_PUBLISHPROGRESS"+Arrays.toString(walletitems));
            items.add(new SongItems(R.drawable.tky_tokenly_album, walletitems[0].getName(), walletitems[0].getComposers(),SongStatus.DED.getCode(),walletitems[0].getSongId(),0,false));
            adapter.setFilter(items);
        }


        /**
         * afte finish receive the value of doinbackground
         */

        protected void onPostExecute(Boolean ready) {

            swipeContainer.setRefreshing(false);
            Log.v(TAG, "Game Over MonitoringThreadClass");

        }
        /**
         * when the method cancel is called
         */
        @Override
        protected void onCancelled() {

        }





    }


    //JUST FOR TEST


    private Fan getTestFanIdentity(){
        Fan fanIdentity = new Fan() {


            @Override
            public String getTokenlyId() {
                return null;
            }

            @Override
            public String getUsername() {
                return null;
            }

            @Override
            public String getEmail() {
                return null;
            }

            @Override
            public String getApiToken() {
                return null;
            }

            @Override
            public String getApiSecretKey() {
                return null;
            }

            @Override
            public UUID getId() {
                return null;
            }

            @Override
            public String getPublicKey() {
                return null;
            }

            @Override
            public byte[] getProfileImage() {
                return new byte[0];
            }

            @Override
            public void setNewProfileImage(byte[] imageBytes) {

            }


            @Override
            public ExternalPlatform getExternalPlatform() {
                return null;
            }

            @Override
            public MusicUser getMusicUser() {
                MusicUser hardocedUser = new MusicUser() {

                    @Override
                    public String getTokenlyId() {
                        return null;
                    }

                    @Override
                    public String getUsername() {
                        return "pereznator";
                    }

                    @Override
                    public String getEmail() {
                        return "darkpriestrelative@gmail.com";
                    }

                    @Override
                    public String getApiToken() {
                        return "Tvn1yFjTsisMHnlI";
                    }

                    @Override
                    public String getApiSecretKey() {
                        return "K0fW5UfvrrEVQJQnK27FbLgtjtWHjsTsq3kQFB6Y";
                    }
                };
                return hardocedUser;
            }

            @Override
            public String getUserPassword() {
                return null;
            }
        };
        return fanIdentity;
    }








}
