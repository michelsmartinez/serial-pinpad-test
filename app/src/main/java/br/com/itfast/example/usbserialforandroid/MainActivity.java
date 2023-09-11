package br.com.itfast.example.usbserialforandroid;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private UsbSerialDriver driver = null;
    private UsbManager manager = null;
    private final String TAG = "SERIAL_TEST";

    private void sendCommand(String command, UsbSerialPort port) throws IOException {
        port.write(command.getBytes(StandardCharsets.UTF_8), 5000);
        byte[] response = new byte[1000];
        int len = port.read(response, 5000);
        Log.d(TAG, "command  len =" + len);
        Log.d(TAG, "response = " + Arrays.toString(response));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            // Find all available drivers from attached devices.
            Log.d(TAG, "1");
            manager = (UsbManager) getSystemService(Context.USB_SERVICE);
            Log.d(TAG, "manager = " + manager.toString());
            for (final UsbDevice device : manager.getDeviceList().values()) {
                Log.d(TAG, "VID : " + device.getVendorId() + "PID : " + device.getProductId());
            }
            List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
            if (availableDrivers.isEmpty()) {
                return;
            }

            for (int i = 0; i < availableDrivers.size(); i++) {
                if (
                        availableDrivers.get(i).getDevice().getManufacturerName().toUpperCase().contains("GERTEC") ||
                                availableDrivers.get(i).getDevice().getManufacturerName().toUpperCase().contains("PPC") ||
                                availableDrivers.get(i).getDevice().getManufacturerName().toUpperCase().contains("VERIFONE") ||
                                availableDrivers.get(i).getDevice().getManufacturerName().toUpperCase().contains("VX820") ||
                                availableDrivers.get(i).getDevice().getManufacturerName().toUpperCase().contains("VX 820") ||
                                availableDrivers.get(i).getDevice().getManufacturerName().toUpperCase().contains("INGENICO")
                ) {
                    driver = availableDrivers.get(i);
                    break;
                }
            }
        } catch (Exception ex) {
            Log.e("ITFAST", ex.getMessage());
        }

        Button btnIdentificar = findViewById(R.id.btnIdentificarDispositivo);
        btnIdentificar.setOnClickListener(v -> {
            if (driver != null) {
                Toast.makeText(MainActivity.this, "PINPAD " + driver.getDevice().getManufacturerName() + " " + driver.getDevice().getProductName() + " identificado", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(MainActivity.this, "Nenhum dos PINPADs mapeados foi identificado", Toast.LENGTH_LONG).show();
            }
        });

        Button btnAbrirComunicacao = findViewById(R.id.btnAbrirComunicacao);
        btnAbrirComunicacao.setOnClickListener(v -> {
            try {
                if (driver == null) {
                    Toast.makeText(MainActivity.this, "Nenhum dos PINPADs mapeados foi identificado", Toast.LENGTH_LONG).show();
                    return;
                }
//                manager.requestPermission(driver.getDevice(), mPermissionIntent);

                UsbDeviceConnection connection = manager.openDevice(driver.getDevice());
                if (connection == null) {
                    Toast.makeText(MainActivity.this, "Não foi possível abrir comunicação com o PINPAD", Toast.LENGTH_LONG).show();
                    // add UsbManager.requestPermission(driver.getDevice(), ..) handling here
                    return;
                }

                UsbSerialPort port = driver.getPorts().get(0); // Most devices have just one port (port 0)
                port.open(connection);
                port.setParameters(19200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);

                sendCommand("OPN", port);
                sendCommand("GIN00", port);

                Toast.makeText(MainActivity.this, "Comunicação com PINPAD aberta com sucesso", Toast.LENGTH_LONG).show();
            } catch (Exception ex) {
                Log.e(TAG, ex.getMessage(), ex);
                Toast.makeText(MainActivity.this, "Erro ao abrir comunicação com o PINPAD", Toast.LENGTH_LONG).show();
            }
        });
    }
}