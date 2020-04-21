package net.runee.gui.components;

import com.jgoodies.forms.builder.FormBuilder;
import jouvieje.bass.Bass;
import jouvieje.bass.structures.BASS_DEVICEINFO;
import net.dv8tion.jda.api.JDA;
import net.runee.DiscordAudioStreamBot;
import net.runee.errors.BassException;
import net.runee.gui.renderer.PlaybackDeviceListCellRenderer;
import net.runee.gui.renderer.RecordingDeviceListCellRenderer;
import net.runee.gui.listitems.PlaybackDeviceItem;
import net.runee.misc.Utils;
import net.runee.misc.gui.SpecBuilder;
import net.runee.gui.listitems.RecordingDeviceItem;
import net.runee.model.Config;

import javax.swing.*;
import java.io.IOException;
import java.util.Objects;

public class SettingsPanel extends JPanel {
    // general
    private JTextField botToken;

    // audio
    private JCheckBox speakEnabled;
    private JCheckBox listenEnabled;
    private JList<RecordingDeviceItem> recordingDevices;
    private JList<PlaybackDeviceItem> playbackDevices;

    public SettingsPanel() {
        initComponents();
        layoutComponents();
        loadConfig();
    }

    private void initComponents() {
        final DiscordAudioStreamBot bot = DiscordAudioStreamBot.getInstance();

        // general
        botToken = new JTextField();
        Utils.addChangeListener(botToken, e -> {
            bot.getConfig().botToken = Utils.emptyStringToNull(((JTextField) e.getSource()).getText());
            saveConfig();
        });

        // audio
        speakEnabled = new JCheckBox();
        speakEnabled.addActionListener(e -> {
            boolean speakEnabled = ((JCheckBox) e.getSource()).isSelected();
            bot.setSpeakEnabled(speakEnabled);
            recordingDevices.setEnabled(speakEnabled);
            bot.getConfig().speakEnabled = speakEnabled;
            saveConfig();
        });
        listenEnabled = new JCheckBox();
        listenEnabled.addActionListener(e -> {
            boolean listenEnabled = ((JCheckBox) e.getSource()).isSelected();
            bot.setListenEnabled(listenEnabled);
            playbackDevices.setEnabled(listenEnabled);
            bot.getConfig().listenEnabled = listenEnabled;
            saveConfig();
        });
        recordingDevices = new JList<>();
        recordingDevices.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        recordingDevices.setCellRenderer(new RecordingDeviceListCellRenderer());
        recordingDevices.addListSelectionListener(e -> {
            if (recordingDevices.getSelectedIndex() >= 0) {
                RecordingDeviceItem value = recordingDevices.getSelectedValue();
                String recordingDevice = value != null ? value.getName() : null;
                bot.setRecordingDevice(recordingDevice);
                bot.getConfig().recordingDevice = recordingDevice;
                saveConfig();
            }
        });
        playbackDevices = new JList<>();
        playbackDevices.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        playbackDevices.setCellRenderer(new PlaybackDeviceListCellRenderer());
        playbackDevices.addListSelectionListener(e -> {
            if (playbackDevices.getSelectedIndex() >= 0) {
                PlaybackDeviceItem value = playbackDevices.getSelectedValue();
                String playbackDevice = value != null ? value.getName() : null;
                bot.setPlaybackDevice(playbackDevice);
                bot.getConfig().playbackDevice = playbackDevice;
                saveConfig();
            }
        });
    }

    private void loadConfig() {
        final Config config = DiscordAudioStreamBot.getInstance().getConfig();

        // general
        botToken.setText(Utils.nullToEmptyString(config.botToken));

        // audio
        speakEnabled.setSelected(config.getSpeakEnabled());
        listenEnabled.setSelected(config.getListenEnabled());
        recordingDevices.setEnabled(speakEnabled.isSelected());
        {
            DefaultListModel<RecordingDeviceItem> model = new DefaultListModel<>();
            //model.addElement(null);
            BASS_DEVICEINFO info = BASS_DEVICEINFO.allocate();
            for (int device = 0; Bass.BASS_RecordGetDeviceInfo(device, info); device++) {
                model.addElement(new RecordingDeviceItem(info.getName(), device));
            }
            info.release();
            recordingDevices.setModel(model);
            for (int i = 0; i < model.getSize(); i++) {
                RecordingDeviceItem recordingDevice = model.get(i);
                String recordingDeviceName = recordingDevice != null ? recordingDevice.getName() : null;
                if (Objects.equals(recordingDeviceName, config.recordingDevice)) {
                    recordingDevices.setSelectedIndex(i);
                    break;
                }
            }
        }
        playbackDevices.setEnabled(listenEnabled.isSelected());
        {
            DefaultListModel<PlaybackDeviceItem> model = new DefaultListModel<>();
            //model.addElement(null);
            BASS_DEVICEINFO info = BASS_DEVICEINFO.allocate();
            for (int device = 0; Bass.BASS_GetDeviceInfo(device, info); device++) {
                model.addElement(new PlaybackDeviceItem(info.getName(), device));
            }
            info.release();
            playbackDevices.setModel(model);
            for (int i = 0; i < model.getSize(); i++) {
                PlaybackDeviceItem playbackDeviceItem = model.get(i);
                String playbackDeviceName = playbackDeviceItem != null ? playbackDeviceItem.getName() : null;
                if (Objects.equals(playbackDeviceName, config.playbackDevice)) {
                    playbackDevices.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private void saveConfig() {
        try {
            DiscordAudioStreamBot.getInstance().saveConfig();
        } catch (IOException ex) {
            Utils.guiError(this, "Failed to save config.", ex);
        }
    }

    private void layoutComponents() {
        int row = 1;
        FormBuilder
                .create()
                .columns(SpecBuilder
                        .create()
                        .add("r:p")
                        .add("f:max(p;100px)")
                        .gap("f:3dlu:g")
                        .add("r:p")
                        .add("f:max(p;100px)")
                        .build()
                )
                .rows(SpecBuilder
                        .create()
                        .add("c:p") // general
                        .add("c:p", 1)
                        .gapUnrelated().add("c:p")
                        .add("c:p", 2)
                        .build()
                )
                .columnGroups(new int[]{1, 5}, new int[]{2, 6})
                .panel(this)
                .border(BorderFactory.createEmptyBorder(5, 5, 5, 5))
                .addSeparator("General").xyw(1, row, 7)
                .add("Bot token").xy(1, row += 2)
                /**/.add(botToken).xyw(3, row, 5)
                .addSeparator("Audio").xyw(1, row += 2, 7)
                .add("Speaking enabled").xy(1, row += 2)
                /**/.add(speakEnabled).xy(3, row)
                /**/.add("Listening enabled").xy(5, row)
                /**/.add(listenEnabled).xy(7, row)
                .add("Recording device").xy(1, row += 2)
                /**/.add(recordingDevices).xy(3, row)
                /**/.add("Playback device").xy(5, row)
                /**/.add(playbackDevices).xy(7, row)
                .build();
    }

    public void updateLoginStatus(JDA.Status status) {
        switch (status) {
            case SHUTDOWN:
            case FAILED_TO_LOGIN:
                botToken.setEnabled(true);
                break;
            default:
                botToken.setEnabled(false);
                break;
        }
    }
}
