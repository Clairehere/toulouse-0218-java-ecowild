package fr.wildcodeschool.ecowild;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    //Pour recup la carte utiliser ses 2 attributs
    //1er name ds layout=instance
    //2eme: objet a linterieur pour pouvoir modif les données
    private GoogleMap mMap;
    DrawerLayout drawerLayout;
    final ArrayList<ElemntModel> gps = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //Snack
        Snackbar snackbar = Snackbar.make(this.findViewById(R.id.map), R.string.snack, Snackbar.LENGTH_INDEFINITE).setDuration(4000).setAction("Connexion", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapsActivity.this, ConnectionActivity.class);
                startActivity(intent);

            }
        });

        View snackbarView = snackbar.getView();
        TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setMaxLines(3);
        snackbar.show();


        //Toast réutilisable plus tard
        /**LayoutInflater inflater = getLayoutInflater();
         View layout = inflater.inflate(R.layout.toast,
         (ViewGroup) findViewById(R.id.custom_toast_container));

         TextView textToast = (TextView) layout.findViewById(R.id.text);
         textToast.setText(R.string.Toast);

         Toast toast = new Toast(getApplicationContext());
         toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
         toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
         toast.setDuration(Toast.LENGTH_LONG);
         toast.setView(layout);
         toast.show();
         **/

        //Map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // slide droite et gauche
        drawerLayout = findViewById(R.id.drawerLayout);
        Button buttonLeft = findViewById(R.id.button_left);

        buttonLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(Gravity.LEFT);
            }
        });

        Button buttonRight = findViewById(R.id.button_right);
        buttonRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(Gravity.RIGHT);
            }
        });

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        googleMap.setMyLocationEnabled(true);

        LatLng departure = new LatLng(43.6043896, 1.4433718000000226);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(departure, 10));

        // filtre afin de n'afficher que certaines données, en lien avec le json dans raw (créé).
        MapStyleOptions mapFilter = MapStyleOptions.loadRawResourceStyle(MapsActivity.this, R.raw.map_style);
        googleMap.setMapStyle(mapFilter);

        /** Partie Json Verre**/

        final TextView testPosition = findViewById(R.id.test_position);

        // Crée une file d'attente pour les requêtes vers l'API
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        String url = "https://data.toulouse-metropole.fr/api/records/1.0/search/?dataset=recup-verre&refine.commune=TOULOUSE";

        // Création de la requête vers l'API, ajout des écouteurs pour les réponses et erreurs possibles
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            JSONArray records = response.getJSONArray("records");

                            for (int c = 0; c < records.length(); c++) {
                                JSONObject recordslist = records.getJSONObject(c);
                                JSONObject geometry = recordslist.getJSONObject("geometry");
                                JSONObject location = recordslist.getJSONObject("fields");
                                String address = location.getString("adresse");
                                JSONArray coordinate = geometry.getJSONArray("coordinates");
                                String abs = coordinate.getString(0);
                                String ordo = coordinate.getString(1);
                                double valueAbs = Double.parseDouble(abs);
                                double valueOrdo = Double.parseDouble(ordo);
                                String type = "Verre";
                                String id = "v" + c;

                                gps.add(new ElemntModel(address,type,id));

                                // testPosition.append(valueAbs + " " + valueOrdo + address+ " \n ");
                                mMap.addMarker(new MarkerOptions().position(new LatLng(valueOrdo, valueAbs)).title(address)
                                        .snippet(type).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Afficher l'erreur
                        Log.d("VOLLEY_ERROR", "onErrorResponse: " + error.getMessage());
                    }
                }
        );

        // On ajoute la requête à la file d'attente
        requestQueue.add(jsonObjectRequest);

        /** Partie Json Papier/plastique **/
        // Crée une file d'attente pour les requêtes vers l'API
        RequestQueue requestQueueTwo = Volley.newRequestQueue(this);

        String urlTwo = "https://data.toulouse-metropole.fr/api/records/1.0/search/?dataset=recup-emballage&refine.commune=TOULOUSE";

        // Création de la requête vers l'API, ajout des écouteurs pour les réponses et erreurs possibles
        JsonObjectRequest jsonObjectRequestTwo = new JsonObjectRequest(
                Request.Method.GET, urlTwo, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            JSONArray records = response.getJSONArray("records");

                            for (int c = 0; c < records.length(); c++) {
                                JSONObject recordslist = records.getJSONObject(c);
                                JSONObject geometry = recordslist.getJSONObject("geometry");
                                JSONObject location = recordslist.getJSONObject("fields");
                                String address = location.getString("adresse");
                                JSONArray coordinate = geometry.getJSONArray("coordinates");
                                String abs = coordinate.getString(0);
                                String ordo = coordinate.getString(1);
                                double valueAbs = Double.parseDouble(abs);
                                double valueOrdo = Double.parseDouble(ordo);
                                String type = "Papier/Plastique";
                                String id = "p" + c;

                                gps.add(new ElemntModel(address,type,id));

                                // testPosition.append(valueAbs + " " + valueOrdo + address + " \n ");
                                mMap.addMarker(new MarkerOptions().position(new LatLng(valueOrdo, valueAbs)).title(address)
                                        .snippet(type).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Afficher l'erreur
                        Log.d("VOLLEY_ERROR", "onErrorResponse: " + error.getMessage());
                    }
                }
        );

        // On ajoute la requête à la file d'attente
        requestQueueTwo.add(jsonObjectRequestTwo);

        Switch goList = findViewById(R.id.golist);
        goList.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Intent goList = new Intent(MapsActivity.this, ListLocationActivity.class);
                goList.putExtra("CLEF", gps);
                startActivity(goList);

            }
        });
    }
}

