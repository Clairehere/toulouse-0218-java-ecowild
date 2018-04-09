package fr.wildcodeschool.ecowild;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
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

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {


    //Pour recup la carte utiliser ses 2 attributs
    //1er name ds layout=instance
    //2eme: objet a linterieur pour pouvoir modif les données
    private GoogleMap mMap;
    int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Switch goList = findViewById(R.id.golist);
        goList.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Intent goToList = new Intent(MapsActivity.this, ListLocationActivity.class);
                startActivity(goToList);
            }
        });


    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        googleMap.setMyLocationEnabled(true);

        LatLng depart = new LatLng(43.6043896, 1.4433718000000226);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(depart, 10));

        // filtre afin de n'afficher que certaines données, en lien avec le json dans raw (créé).
        MapStyleOptions mapFilter = MapStyleOptions.loadRawResourceStyle(MapsActivity.this, R.raw.map_style);
        googleMap.setMapStyle(mapFilter);

        // Add a marker in Sydney and move the camera
        //mMap.addMarker(new MarkerOptions().position(depart).title("Marker in Sydney"));


        /** Partie Json Verre**/

        final TextView testPosition = findViewById(R.id.test_position);

        // Crée une file d'attente pour les requêtes vers l'API
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        // TODO : URL de la requête vers l'API
        String url = "https://data.toulouse-metropole.fr/api/records/1.0/search/?dataset=recup-verre&refine.commune=TOULOUSE";

        // Création de la requête vers l'API, ajout des écouteurs pour les réponses et erreurs possibles
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            JSONArray records = response.getJSONArray("records");

                            for (int c =0; c < records.length(); c++) {
                                JSONObject recordslist = records.getJSONObject(c);
                                JSONObject geometry = recordslist.getJSONObject("geometry");
                                JSONObject location = recordslist.getJSONObject("fields");
                                String adress = location.getString("adresse");
                                JSONArray coordonates = geometry.getJSONArray("coordinates");
                                String abs = coordonates.getString(0);
                                String ordo = coordonates.getString(1);
                                double valueAbs = Double.parseDouble(abs);
                                double valueOrdo = Double.parseDouble(ordo);
                                String type = "Verre";

                                //TODO faire constructeur pour points

                                // testPosition.append(valueAbs + " " + valueOrdo + adress+ " \n ");
                                mMap.addMarker(new MarkerOptions().position(new LatLng(valueOrdo, valueAbs)).title(adress)
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

        // TODO : URL de la requête vers l'API
        String urlTwo = "https://data.toulouse-metropole.fr/api/records/1.0/search/?dataset=recup-emballage&refine.commune=TOULOUSE";

        // Création de la requête vers l'API, ajout des écouteurs pour les réponses et erreurs possibles
        JsonObjectRequest jsonObjectRequestTwo = new JsonObjectRequest(
                Request.Method.GET, urlTwo, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            JSONArray records = response.getJSONArray("records");

                            for (int c =0; c < records.length(); c++) {
                                JSONObject recordslist = records.getJSONObject(c);
                                JSONObject geometry = recordslist.getJSONObject("geometry");
                                JSONObject location = recordslist.getJSONObject("fields");
                                String adress = location.getString("adresse");
                                JSONArray coordonates = geometry.getJSONArray("coordinates");
                                String abs = coordonates.getString(0);
                                String ordo = coordonates.getString(1);
                                double valueAbs = Double.parseDouble(abs);
                                double valueOrdo = Double.parseDouble(ordo);
                                String type = "Papier/Plastique";

                                //TODO faire constructeur pour points

                                // testPosition.append(valueAbs + " " + valueOrdo + adress+ " \n ");
                                mMap.addMarker(new MarkerOptions().position(new LatLng(valueOrdo, valueAbs)).title(adress)
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

    }
}

