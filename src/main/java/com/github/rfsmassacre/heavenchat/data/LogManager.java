package com.github.rfsmassacre.heavenchat.data;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class LogManager
{
    private final JavaPlugin instance;
    private final String folderName;

    public LogManager(JavaPlugin instance, String folderName)
    {
        this.instance = instance;
        this.folderName = folderName;

        //Create folder if needed
        File folder = new File(instance.getDataFolder().getAbsolutePath() + File.separator + folderName);
        if (!folder.exists())
        {
            folder.mkdirs();
        }
    }

    public void write(String fileName, List<String> lines)
    {
        if (lines == null || lines.isEmpty())
        {
            return;
        }

        try
        {
            File file = new File(instance.getDataFolder().getAbsolutePath() + File.separator + folderName +
                    File.separator + fileName);
            if (!file.exists())
            {
                file.createNewFile();
            }

            FileWriter writer = new FileWriter(file, true);
            for (String line : lines)
            {
                writer.write(line + System.lineSeparator());
            }

            writer.flush();
            writer.close();
        }
        catch (IOException exception)
        {
            //Do nothing
        }
    }
}
