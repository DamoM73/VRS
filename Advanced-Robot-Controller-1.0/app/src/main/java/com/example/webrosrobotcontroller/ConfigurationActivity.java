package com.example.webrosrobotcontroller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.qualcomm.robotcore.hardware.DcMotorImpl;

import org.json.JSONArray;
import org.json.JSONObject;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ConfigurationActivity extends AppCompatActivity implements NewConfigurationDialog.NewConfigurationDialogListener {

    String activeConfigurationName;
    TextView activeConfigurationNameLabel;
    ArrayList<String> existingConfigurationNames;
    ListView listView;
    ConfigurationListAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration);

        activeConfigurationName = getIntent().getStringExtra("ACTIVE_CONFIGURATION_NAME");
        activeConfigurationNameLabel = findViewById(R.id.activeConfigurationNameLabel);
        activeConfigurationNameLabel.setText(activeConfigurationName);

        Button newConfiguration = findViewById(R.id.newConfigurationButton);
        newConfiguration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NewConfigurationDialog configurationDialog = new NewConfigurationDialog();
                configurationDialog.show(getSupportFragmentManager(), "newConfigDialog");
            }
        });
        existingConfigurationNames = new ArrayList<>();

        Button configureFromTemplateButton = findViewById(R.id.configurationTemplatesButton);
        configureFromTemplateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapter.notifyDataSetChanged();
                for (int i = 0; i < existingConfigurationNames.size(); i++) {
                    if (existingConfigurationNames.get(i).equals("defaultRobot")) {
                        Toast.makeText(ConfigurationActivity.this, "Default configuration already exists.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                existingConfigurationNames.add("defaultRobot");
                try {
                    JSONObject jsonObject = new JSONObject();
                    JSONArray jsonArray = new JSONArray();
                    for (int i = 0; i < existingConfigurationNames.size(); i++) {
                        jsonArray.put(existingConfigurationNames.get(i));
                    }
                    jsonObject.put("configurations", jsonArray);
                    writeFileOnInternalStorage(ConfigurationActivity.this, "configurations.txt", jsonObject.toString());
                } catch (Exception ignore) {

                }
                adapter.notifyDataSetChanged();

                try {
                    JSONObject jsonObject = new JSONObject();
                    JSONArray jsonArray = new JSONArray();

                    jsonArray.put("frontLeft");
                    jsonArray.put("frontRight");
                    jsonArray.put("backLeft");
                    jsonArray.put("backRight");
                    jsonArray.put("intake");
                    jsonArray.put("hopper");
                    jsonArray.put("leftShooter");
                    jsonArray.put("rightShooter");

                    jsonObject.put("devices", jsonArray);
                    writeFileOnInternalStorage(ConfigurationActivity.this, "defaultRobot" + ".txt", jsonObject.toString());
                } catch (Exception ignore) {
                }

            }
        });

        try {
            StringBuilder sb = new StringBuilder();
            FileInputStream fis = new FileInputStream(new File(this.getFilesDir() + "/" + "configurations.txt"));
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader buff = new BufferedReader(isr);

            String line;
            while ((line = buff.readLine()) != null) {
                sb.append(line + "\n");
            }

            fis.close();
            JSONObject allConfigs = new JSONObject(sb.toString());

            for (int i = 0; i < allConfigs.getJSONArray("configurations").length(); i++) {
                existingConfigurationNames.add(allConfigs.getJSONArray("configurations").get(i).toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Button doneButton = findViewById(R.id.doneButtonConfigurationMenu);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        listView = findViewById(R.id.configurationListView);
        adapter = new ConfigurationListAdapter(this, R.layout.configuration_listview_item, existingConfigurationNames);
        listView.setAdapter(adapter);
    }

    @Override
    public void createNewConfiguration(String name) {

        for (int i = 0; i < existingConfigurationNames.size(); i++) {
            if (existingConfigurationNames.get(i).equals(name) || existingConfigurationNames.get(i).equals("configurations") || existingConfigurationNames.get(i).equals("activeConfigs")) {
                Toast.makeText(this, "Configuration with same name already exists.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        existingConfigurationNames.add(name);
        try {
            JSONObject jsonObject = new JSONObject();
            JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < existingConfigurationNames.size(); i++) {
                jsonArray.put(existingConfigurationNames.get(i));
            }
            jsonObject.put("configurations", jsonArray);
            writeFileOnInternalStorage(this, "configurations.txt", jsonObject.toString());
        } catch (Exception ignore) {

        }
        adapter.notifyDataSetChanged();

    }

    public void writeFileOnInternalStorage(Context context, String fileName, String fileContent) {
        try {
            File file = new File(context.getFilesDir(), fileName);
            FileWriter writer = new FileWriter(file);
            writer.append(fileContent);
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class ConfigurationListAdapter extends ArrayAdapter<String> {
        private int layout;
        private List<String> allConfigurationNames;

        public ConfigurationListAdapter(@NonNull Context context, int resource, @NonNull List<String> objects) {
            super(context, resource, objects);
            layout = resource;
            allConfigurationNames = objects;
        }

        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            ConfigurationListViewHolder mainViewHolder;
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(layout, parent, false);
                ConfigurationListViewHolder viewHolder = new ConfigurationListViewHolder();
                viewHolder.configurationName = convertView.findViewById(R.id.configurationListViewNameText);
                viewHolder.editConfiguration = convertView.findViewById(R.id.configurationListViewEditButton);
                viewHolder.activateConfiguration = convertView.findViewById(R.id.configurationListViewActivateButton);
                viewHolder.deleteConfiguration = convertView.findViewById(R.id.configurationListViewDeleteButton);

                viewHolder.editConfiguration.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(ConfigurationActivity.this, ConfigurationEditor.class);
                        intent.putExtra("CONFIGURATION_NAME", allConfigurationNames.get(position));
                        intent.putExtra("ACTIVE_CONFIGURATION_NAME", activeConfigurationName);
                        startActivity(intent);
                    }
                });

                viewHolder.activateConfiguration.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        activeConfigurationName = allConfigurationNames.get(position);
                        activeConfigurationNameLabel.setText(activeConfigurationName);
                        writeFileOnInternalStorage(getContext(), "activeConfig.txt", activeConfigurationName);
                        try {
                            StringBuilder sb = new StringBuilder();
                            FileInputStream fis = new FileInputStream(new File(getFilesDir() + "/" + activeConfigurationName + ".txt"));
                            InputStreamReader isr = new InputStreamReader(fis);
                            BufferedReader buff = new BufferedReader(isr);

                            String line;
                            while ((line = buff.readLine()) != null) {
                                sb.append(line + "\n");
                            }

                            fis.close();
                            JSONObject allConfigs = new JSONObject(sb.toString());

                            String motor1Name = allConfigs.getJSONArray("devices").get(0).toString();
                            String motor2Name = allConfigs.getJSONArray("devices").get(1).toString();
                            String motor3Name = allConfigs.getJSONArray("devices").get(2).toString();
                            String motor4Name = allConfigs.getJSONArray("devices").get(3).toString();
                            String motor5Name = allConfigs.getJSONArray("devices").get(4).toString();
                            String motor6Name = allConfigs.getJSONArray("devices").get(5).toString();
                            String motor7Name = allConfigs.getJSONArray("devices").get(6).toString();
                            String motor8Name = allConfigs.getJSONArray("devices").get(7).toString();
                            String newActivityConfigFile = "[{\"frc\": \"" + motor1Name + "\", \"sim\": \"motor1\"},{\"frc\": \"" + motor2Name + "\", \"sim\": \"motor2\"},{\"frc\": \"" + motor3Name + "\", \"sim\": \"motor3\"},{\"frc\": \"" + motor4Name + "\", \"sim\": \"motor4\"},{\"frc\": \"" + motor5Name + "\", \"sim\": \"motor5\"},{\"frc\": \"" + motor6Name + "\", \"sim\": \"motor6\"},{\"frc\": \"" + motor7Name + "\", \"sim\": \"motor7\"},{\"frc\": \"" + motor8Name + "\", \"sim\": \"motor8\"}]";

                            writeFileOnInternalStorage(getContext(), "activeConfiguration.txt", newActivityConfigFile);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        try {
                            StringBuilder sb = new StringBuilder();
                            FileInputStream fis = new FileInputStream(new File(getFilesDir() + "/" + activeConfigurationName + ".txt"));
                            InputStreamReader isr = new InputStreamReader(fis);
                            BufferedReader buff = new BufferedReader(isr);
                            String line;
                            while ((line = buff.readLine()) != null) {
                                sb.append(line + "\n");
                            }
                            fis.close();

                            DcMotorImpl.activeConfigContent = sb.toString();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                });

                viewHolder.deleteConfiguration.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (allConfigurationNames.get(position).equals(activeConfigurationName)) {
                            activeConfigurationName = "";
                            activeConfigurationNameLabel.setText(activeConfigurationName);
                            writeFileOnInternalStorage(getContext(), "activeConfig.txt", activeConfigurationName);
                        }
                        String configName = allConfigurationNames.get(position);
                        allConfigurationNames.remove(position);
                        try {
                            JSONObject jsonObject = new JSONObject();
                            JSONArray jsonArray = new JSONArray();
                            for (int i = 0; i < allConfigurationNames.size(); i++) {
                                jsonArray.put(allConfigurationNames.get(i));
                            }
                            jsonObject.put("configurations", jsonArray);
                            writeFileOnInternalStorage(ConfigurationActivity.this, "configurations.txt", jsonObject.toString());
                        } catch (Exception ignore) {

                        }
                        notifyDataSetChanged();
                        File fileToDelete = new File(ConfigurationActivity.this.getFilesDir() + "/" + configName + ".txt");
                        if (fileToDelete.exists()) {
                            if (fileToDelete.delete()) {
                                Toast.makeText(ConfigurationActivity.this, configName + " Successfully Deleted", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(ConfigurationActivity.this, "Unsuccessful Deletion", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(ConfigurationActivity.this, configName + " Successfully Deleted", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
                viewHolder.configurationName.setText(getItem(position));
                convertView.setTag(viewHolder);
            } else {
                mainViewHolder = (ConfigurationListViewHolder) convertView.getTag();
                mainViewHolder.configurationName.setText(getItem(position));
            }

            return convertView;
        }

        @Nullable
        @Override
        public String getItem(int position) {
            return allConfigurationNames.get(position);
        }
    }

    public class ConfigurationListViewHolder {
        TextView configurationName;
        Button editConfiguration;
        Button activateConfiguration;
        Button deleteConfiguration;
    }
}