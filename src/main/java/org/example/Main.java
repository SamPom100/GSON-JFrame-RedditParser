package org.example;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.example.template.Child;
import org.example.template.RedditObject;
import com.google.gson.Gson;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) throws Exception {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JTextField textField = new JTextField();
        JCheckBox checkBox = new JCheckBox("Save to desktop");
        panel.add(textField);
        panel.add(checkBox);
        JOptionPane.showMessageDialog(null, panel, "Enter a subreddit", JOptionPane.PLAIN_MESSAGE);

        boolean saveToDesktop = checkBox.isSelected();
        String URL = "https://www.reddit.com/r/" + textField.getText() + "/.json";
        RedditObject obj = getObjFromURL(URL);

        ArrayList<String> resultList = new ArrayList<>();
        for (Child name : obj.getData().getChildren()) {
            resultList.add(name.getData().getUrl_overridden_by_dest());
        }

        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        for (String url : resultList) {
            executor.execute(() -> {
                DisplayImage(url, saveToDesktop);
            });
        }
        executor.shutdown();
    }

    public static RedditObject getObjFromURL(String in) {
        try {
            URLConnection connection = new URL(in).openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.connect();
            InputStream inStream = connection.getInputStream();
            InputStreamReader isReader = new InputStreamReader(inStream);
            JsonElement root = JsonParser.parseReader(isReader);
            Gson gson = new Gson();
            return gson.fromJson(root, org.example.template.RedditObject.class);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void DisplayImage(String input, boolean saveToDesktop) {
        if (input != null) {
            try {
                ImageIO.setUseCache(false);
                Image img = ImageIO.read(new URL(input));
                if (saveToDesktop) {
                    String desktopPath = System.getProperty("user.home") + "/Desktop";
                    File dir = new File(desktopPath + "/RedditImages");
                    if (!dir.exists()) {
                        dir.mkdir();
                    }
                    File file = new File(dir, input.substring(input.lastIndexOf("/") + 1));
                    ImageIO.write((RenderedImage) img, "jpg", file);
                }

                int height = img.getHeight(null);
                int width = img.getWidth(null);

                if (height > 900) {
                    double ratio = 900.0 / height;
                    height = (int) (height * ratio);
                    width = (int) (width * ratio);
                    img = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                }

                JFrame frame = new JFrame();
                frame.setSize(width, height);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.getContentPane().setLayout(new FlowLayout());
                frame.getContentPane().add(new JLabel(new ImageIcon(img)));
                frame.setVisible(true);

            } catch (Exception exp) {
                exp.printStackTrace();
            }
        }
    }
}