package ru.ifmo.rain.pakulev.walk;

import java.io.*;

public class Walk {
    public static void main(String[] args) {
        if (args == null || (args.length > 1 && (args[0] == null || args[1] == null))) {
            System.err.println("Unsupported arguments");
            return;
        }

        if (args.length == 0) {
            System.err.println("No arguments, expected two");
            return;
        }

        if (args.length == 1) {
            System.err.println("Only one argument, expected two");
            return;
        }


        try (
                BufferedReader is = new BufferedReader(new InputStreamReader(new FileInputStream(args[0]), "UTF-8"));
                BufferedWriter os = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(args[1]), "UTF-8"));
        ) {
            String name;
            while ((name = is.readLine()) != null) {
                try (InputStream in = new BufferedInputStream(new FileInputStream(name))) {
                    os.write(String.format("%08x", hash(in)) + " " + name + '\n');
                } catch (FileNotFoundException e) {
                    os.write(String.format("%08x", 0) + " " + name + '\n');
                    System.err.println("Can't find the the file or the path: " + name);
                }
            }
        } catch (UnsupportedEncodingException e) {
            System.err.println("Unsupported encoding");
        } catch (FileNotFoundException e) {
            System.err.println("Can't find the path, or invalid path");
        } catch (IOException e) {
            System.err.println(e.toString());
        }
    }

    private static int hash(InputStream file) throws IOException {
        int FNV = 0x01000193, h = 0x811c9dc5, c;
        while ((c = file.read()) >= 0) {
            h = (h * FNV) ^ (c & 0xff);
        }
        return h;
    }
}