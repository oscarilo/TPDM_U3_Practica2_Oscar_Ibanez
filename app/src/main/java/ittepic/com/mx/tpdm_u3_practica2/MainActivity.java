package ittepic.com.mx.tpdm_u3_practica2;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    EditText materia, descripcion;
    Button guardar;

    DatabaseReference realtime;

    List<Nota> listaNotas;

    ListView lista;

    List<String> ides;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        realtime = FirebaseDatabase.getInstance().getReference();

        materia = findViewById(R.id.materia);
        descripcion = findViewById(R.id.descripcion);

        guardar = findViewById(R.id.guardar);

        lista = findViewById(R.id.lista);

        guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (materia.getText().toString().isEmpty() || descripcion.getText().toString().isEmpty()) {
                    mensajes("Llene ambos campos!");
                } else {
                    crearNota();
                }
            }
        });

    }// onCreate

    private void crearNota() {

        // Obtener fecha de nota
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat mdformat = new SimpleDateFormat("dd/MM/yyyy");
        String strDate = mdformat.format(calendar.getTime());

        Nota nota = new Nota(materia.getText().toString(), descripcion.getText().toString(), strDate);

        realtime.child("notas").push().setValue(nota).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                materia.setText("");
                descripcion.setText("");
                materia.requestFocus();
                mensajes("Nota creada con éxito!");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mensajes("Error al crear la nota!");
            }
        });

    }// crearNota

    private void mensajes(String titulo) {
        Toast.makeText(this, titulo, Toast.LENGTH_LONG).show();
    }// mensajes

    @Override
    protected void onStart() {
        super.onStart();
        consultarTodos();
    }// onStart

    private void consultarTodos() {
        realtime.child("notas").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listaNotas = new ArrayList<>();
                ides = new ArrayList<>();

                for (DataSnapshot snap : dataSnapshot.getChildren()) {
                    Nota nota = snap.getValue(Nota.class);

                    if (nota != null) {
                        listaNotas.add(nota);
                        ides.add(snap.getKey());
                    }
                }

                crearListView();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }// consultarTodos

    private void crearListView() {

        if (listaNotas.size() <= 0) {
            return;
        }
        String[] nombres = new String[listaNotas.size()];

        for (int i = 0; i < nombres.length; i++) {
            Nota j = listaNotas.get(i);
            nombres[i] = j.materia + "\n" + j.fecha;
        }

        lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                infoDocumento("" + ides.get(position),
                        listaNotas.get(position).getMateria(),
                        listaNotas.get(position).getDescripcion(),
                        listaNotas.get(position).getFecha());
            }
        });

        ArrayAdapter<String> adaptador = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, nombres);
        lista.setAdapter(adaptador);

    }// crearListView

    private void infoDocumento(final String doc, String materiadate, String descripciondate, String fecha) {
        View vista = getLayoutInflater().inflate(R.layout.plantilla, null);

        final EditText materiaEd = vista.findViewById(R.id.plantilla_materia);
        final EditText descripcionEd = vista.findViewById(R.id.plantilla_descripcion);

        materiaEd.setText(materiadate);
        descripcionEd.setText(descripciondate);
        materiaEd.setSelection(materiaEd.length());

        AlertDialog.Builder alerta = new AlertDialog.Builder(this);
        final Map<String, Object> administrator = new HashMap<>();


        alerta.setTitle("Nota - " + fecha)
                .setNeutralButton("Eliminar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        realtime.child("notas").child(doc).removeValue()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        mensajes("Nota borrada con éxito!");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        mensajes("ERROR AL BORRAR " + e.getMessage());
                                    }
                                });
                    }
                })
                .setPositiveButton("Actualizar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        administrator.put("notas/"+doc+"/materia/", materiaEd.getText().toString());
                        administrator.put("notas/"+doc+"/descripcion/", descripcionEd.getText().toString());

                        realtime.updateChildren(administrator)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        mensajes("Nota actualizada con exito!");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        System.out.println("ERROR " + e.getMessage());
                                    }
                                });
                    }
                })
                .setNegativeButton("Cancelar", null)
                .setView(vista)
                .show();
    }

}// class
