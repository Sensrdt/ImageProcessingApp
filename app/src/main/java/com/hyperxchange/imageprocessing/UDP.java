package com.hyperxchange.imageprocessing;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class UDP extends AppCompatActivity {


    EditText ip, port, msg;
    TextView recieved1;
    Button send;
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_udp);

        ip = findViewById(R.id.ip);
        port = findViewById(R.id.port);
        msg = findViewById(R.id.message);
        recieved1 = findViewById(R.id.recieved);

        send = findViewById(R.id.send);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Intent intent = null;
                            String[] values = new String[5];
                            Socket s = new Socket (ip.getText().toString(), Integer.parseInt(port.getText().toString()));
                            DataOutputStream dos = new DataOutputStream(s.getOutputStream());

                            DataInputStream dis = new DataInputStream(s.getInputStream());

                            String rec = dis.readUTF();
                            String IMEI = null;
                            String[] splits = rec.split(",");
                            int i = 0;
                            for (String a : splits)
                            {
                                values[i]= a;
                                i++;
                            }
                            recieved1.setText(values[1]);

                            Bundle bundle = new Bundle();
                            bundle.putString("message", values[1]);
                            bundle.putString("title", values[0]);

                            intent = new Intent(UDP.this, TakeImage.class);

                            if (intent != null) {
                                intent.putExtras(bundle);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);

                            }

                            dos.writeUTF(msg.getText().toString());
                            dos.flush();
                            dos.close();
                            s.close();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

                t.start();
            }
        });
    }
}
