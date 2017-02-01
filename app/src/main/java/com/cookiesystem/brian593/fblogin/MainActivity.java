package com.cookiesystem.brian593.fblogin;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private TextView info;
    private String dataTok;
    private LoginButton loginButton;
    private static final int SOLICITUD_PERMISO_INTERNET = 1;
    private Intent intentInternet;

    private CallbackManager callbackManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());

        setContentView(R.layout.activity_main);
// Permisos para Android api 23+

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(this, "Tenemos permiso para INTERNET", Toast.LENGTH_SHORT).show();

        } else {


            explicarUsoPermiso();
            solicitarPermisoHacerLlamada();
        }


        // Inicializar Facebook SDK

        // Establecer las devoluciones de llamada
        callbackManager = CallbackManager.Factory.create();
        loginButton = (LoginButton) findViewById(R.id.login_button);

        // Registrar las devoluciones de llamada
        loginButton.registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        info.setText(
                                "User ID: "
                                        + loginResult.getAccessToken().getUserId()
                                        + "\n" +
                                        "Auth Token: "
                                        + loginResult.getAccessToken().
                                        getToken()
                        );

                        makeToast(loginResult.getAccessToken().
                                toString());
                        userData(loginResult.getAccessToken());
                    }



                    @Override
                    public void onCancel() {
                        String cancelMessage = "Login Cancelado.";
                        info.setText(cancelMessage);
                        makeToast(cancelMessage);
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        String errorMessage = "Login error.";
                        info.setText(errorMessage);
                        makeToast(errorMessage);
                    }
                }
        );
        // Inicializar el Texview
        info = (TextView) findViewById(R.id.info);

        if(isLoggedIn())
        {info.setText("User ID: "
                    + AccessToken.getCurrentAccessToken().getUserId());
        userData(AccessToken.getCurrentAccessToken());
        }


    }
    public void userData(AccessToken loginResult)
    {
        GraphRequest request = GraphRequest.newMeRequest(
                loginResult,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(
                            JSONObject object,
                            GraphResponse response) {
                        try {
                            String id = object.getString("id");
                            info.setText(object.getString("name"));
                            String link = object.getString("link");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }



                        // Application code
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,link");
        request.setParameters(parameters);
        request.executeAsync();

    }


    private void explicarUsoPermiso() {


        //Este IF es necesario para saber si el usuario ha marcado o no la casilla [] No volver a preguntar
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CALL_PHONE)) {
            Toast.makeText(this, "2.1 Explicamos razonadamente porque necesitamos el permiso", Toast.LENGTH_SHORT).show();
            //Explicarle al usuario porque necesitas el permiso (Opcional)
            alertDialogBasico();
        }
    }
    public void alertDialogBasico() {


        // 1. Instancia de AlertDialog.Builder con este constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // 2. Encadenar varios métodos setter para ajustar las características del diálogo
        builder.setMessage(R.string.dialog_message);


        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });


        builder.show();

    }
    private void solicitarPermisoHacerLlamada() {



        //Pedimos el permiso o los permisos con un cuadro de dialogo del sistema
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.INTERNET},
                SOLICITUD_PERMISO_INTERNET);

        Toast.makeText(this, "2.2 Pedimos el permiso con un cuadro de dialogo del sistema", Toast.LENGTH_SHORT).show();


    }


    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        /**
         * Si tubieramos diferentes permisos solicitando permisos de la aplicacion, aqui habria varios IF
         */
        if (requestCode == SOLICITUD_PERMISO_INTERNET) {

            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Realizamos la accion
                Toast.makeText(this, "3.1 Permiso Concedido", Toast.LENGTH_SHORT).show();
            } else {
                //1-Seguimos el proceso de ejecucion sin esta accion: Esto lo recomienda Google
                //2-Cancelamos el proceso actual
                //3-Salimos de la aplicacion
                Toast.makeText(this, "3.2 Permiso No Concedido", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public boolean isLoggedIn() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return (accessToken != null) && (!accessToken.isExpired());
    }
    /**
     * datos de interés en el gestor de devolución de llamada
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data)
        ;
    }

    /**
     * creamos los toast
     * @param text
     */
    private void makeToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //los logs de 'instalar' y 'aplicación activa' App Eventos.
        AppEventsLogger.activateApp(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Logs de'app desactivada' App Eventos.
        AppEventsLogger.deactivateApp(this);
    }

}
