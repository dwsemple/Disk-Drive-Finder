
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.LineBorder;

public class DiskDriveFinder extends JFrame implements ActionListener {

    private JTextArea search_query;
    private JScrollPane search_query_scrollpane;
    private GridBagConstraints search_query_gc;
    private JTextArea search_results_display;
    private JScrollPane search_results_scrollpane;
    private GridBagConstraints search_results_display_gc;
    private JButton button_search;
    private GridBagConstraints button_search_gc;
    private JButton button_reset;
    private GridBagConstraints button_reset_gc;
    private JButton button_export;
    private GridBagConstraints button_export_gc;
    private JButton button_exit;
    private GridBagConstraints button_exit_gc;
    private JTextArea system_console_display;
    private JScrollPane system_console_scrollpane;
    private GridBagConstraints system_console_display_gc;
    public String delimiter;
    public Vector<String> pc_list;
    public Vector<String> disk_drive_list;
    public Vector<String> pc_details;
    private Export export_thread;
    private Search search_thread;

    public static void main(String args[]) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new DiskDriveFinder();
            }
        });
    }

    public DiskDriveFinder() {
        super("Disk Drive Finder");

        delimiter = "\t";
        pc_list = new Vector<String>();
        disk_drive_list = new Vector<String>();
        pc_details = new Vector<String>();

        setMinimumSize(new Dimension(1200, 400));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setLayout(new GridBagLayout());

        search_query = new JTextArea("Enter hostnames/IP's here");
        search_query.setBorder(new LineBorder(Color.BLACK, 1));
        search_query_scrollpane = new JScrollPane(search_query);
        search_query_gc = new GridBagConstraints();
        search_query_gc.fill = GridBagConstraints.BOTH;
        search_query_gc.gridx = 0;
        search_query_gc.gridy = 0;
        search_query_gc.gridwidth = 3;
        search_query_gc.weighty = 1;

        search_results_display = new JTextArea("Search Results");
        search_results_display.setEditable(false);
        search_results_display.setBorder(new LineBorder(Color.BLACK, 1));
        search_results_scrollpane = new JScrollPane(search_results_display);
        search_results_display_gc = new GridBagConstraints();
        search_results_display_gc.fill = GridBagConstraints.BOTH;
        search_results_display_gc.gridx = 3;
        search_results_display_gc.gridy = 0;
        search_results_display_gc.gridwidth = GridBagConstraints.REMAINDER;
        search_results_display_gc.weightx = 1;
        search_results_display_gc.weighty = 1;

        button_search = new JButton("Search");
        button_search.setActionCommand("Search");
        button_search.addActionListener(this);
        button_search_gc = new GridBagConstraints();
        button_search_gc.fill = GridBagConstraints.BOTH;
        button_search_gc.gridx = 0;
        button_search_gc.gridy = GridBagConstraints.RELATIVE;
        button_search_gc.gridwidth = 3;
        button_search_gc.gridheight = 1;

        button_reset = new JButton("Reset");
        button_reset.setActionCommand("Reset");
        button_reset.addActionListener(this);
        button_reset_gc = new GridBagConstraints();
        button_reset_gc.fill = GridBagConstraints.BOTH;
        button_reset_gc.gridx = 0;
        button_reset_gc.gridy = GridBagConstraints.RELATIVE;
        button_reset_gc.gridwidth = 1;
        button_reset_gc.gridheight = 1;

        button_export = new JButton("Export");
        button_export.setActionCommand("Export");
        button_export.addActionListener(this);
        button_export_gc = new GridBagConstraints();
        button_export_gc.fill = GridBagConstraints.BOTH;
        button_export_gc.gridx = 1;
        button_export_gc.gridy = GridBagConstraints.RELATIVE;
        button_export_gc.gridwidth = 1;
        button_export_gc.gridheight = 1;

        button_exit = new JButton("Exit");
        button_exit.setActionCommand("Exit");
        button_exit.addActionListener(this);
        button_exit_gc = new GridBagConstraints();
        button_exit_gc.fill = GridBagConstraints.BOTH;
        button_exit_gc.gridx = 2;
        button_exit_gc.gridy = GridBagConstraints.RELATIVE;
        button_exit_gc.gridwidth = 1;
        button_exit_gc.gridheight = 1;

        system_console_display = new JTextArea("System Console");
        system_console_display.setEditable(false);
        system_console_display.setBorder(new LineBorder(Color.BLACK, 1));
        system_console_display.setBackground(Color.BLACK);
        system_console_display.setForeground(Color.WHITE);
        system_console_display.setSelectedTextColor(Color.YELLOW);
        system_console_scrollpane = new JScrollPane(system_console_display);
        system_console_scrollpane.setPreferredSize(new Dimension(1, 1));
        system_console_display_gc = new GridBagConstraints();
        system_console_display_gc.fill = GridBagConstraints.BOTH;
        system_console_display_gc.gridx = GridBagConstraints.RELATIVE;
        system_console_display_gc.gridy = GridBagConstraints.RELATIVE;
        system_console_display_gc.gridheight = 2;
        system_console_display_gc.gridwidth = GridBagConstraints.REMAINDER;
        system_console_display_gc.weightx = 1;

        getContentPane().add(search_query_scrollpane, search_query_gc);
        getContentPane().add(search_results_scrollpane, search_results_display_gc);
        getContentPane().add(button_search, button_search_gc);
        getContentPane().add(system_console_scrollpane, system_console_display_gc);
        getContentPane().add(button_reset, button_reset_gc);
        getContentPane().add(button_export, button_export_gc);
        getContentPane().add(button_exit, button_exit_gc);
        pack();
        setVisible(true);
    }

    private class Export extends SwingWorker<Void, String> {

        @Override
        public Void doInBackground() {
            publish('\n' + "Exporting search results...");
            try {
                String filename = generateFilenameUsingDate("DiskDriveFinder");
                Vector<String> export_details = pc_details;
                export_details.add(0, "PC\tDisk Model\tUsed Space\tFree Space\tTotal Space");
                writeToFile(filename, export_details);
                publish('\n' + "Exported file " + filename);
            } catch (Exception e) {
                publish('\n' + "Export failed");
            }
            button_search.setEnabled(true);
            button_reset.setEnabled(true);
            button_export.setEnabled(true);
            search_query.setEditable(true);
            return null;
        }

        @Override
        protected void process(List<String> lines) {
            for (String line : lines) {
                writeToSystemConsole(line);
            }
        }

        public void writeToFile(String filename, Vector<String> write_to_file) throws IOException {
            try {
                int count = 0;
                File file = new File(filename);
                BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
                for (String string_line : write_to_file) {
                    if (count > 0) {
                        writer.newLine();
                    }
                    writer.write(string_line);
                    count++;
                }
                writer.close();
            } catch (Exception e) {
                throw new IOException();
            }
        }
    }

    private class Search extends SwingWorker<Vector<String>, String> {

        @Override
        public Vector<String> doInBackground() {
            pc_list = readSearchQuery();
            try {
                disk_drive_list = findDetails(pc_list);
            } catch (Exception e) {
                Vector<String> cancelled = new Vector<String>();
                cancelled.add("");
                return cancelled;
            }
            pc_details = compileDetails(pc_list, disk_drive_list, delimiter);
            return pc_details;
        }

        @Override
        protected void process(List<String> lines) {
            for (String line : lines) {
                writeToSystemConsole(line);
            }
        }

        @Override
        public void done() {
            try {
                writeToResultsDisplay(get());
                button_search.setText("Search");
                button_search.setActionCommand("Search");
                button_reset.setEnabled(true);
                button_export.setEnabled(true);
                search_query.setEditable(true);
            } catch (Exception e) {
            }
        }

        public Vector<String> findDetails(Vector<String> pc_list) throws IOException {
            Vector<String> details_list = new Vector<String>();

            int count = 0;
            publish('\n' + "Processing queries (" + pc_list.size() + " total)");
            for (String line : pc_list) {
                try {
                    String detail = "";
                    boolean is_online = pingPC(line);
                    if (is_online) {
                        String command = "wmic /node:" + line + " diskdrive get Caption";
                        ProcessBuilder builder = new ProcessBuilder("C:\\Windows\\System32\\cmd.exe", "/c", command);
                        builder.redirectErrorStream(true);
                        Process p = builder.start();
                        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
                        p.waitFor();

                        boolean search = true;
                        while (search) {
                            detail = r.readLine();
                            if (detail == null) {
                                detail = "No information found";
                                search = false;
                            } else if (detail.contains("Caption")) {
                                detail = r.readLine();
                                detail = r.readLine().trim();

                                String command2 = "wmic /node:" + line + " logicaldisk get size,freespace,caption | find /i \"C:\"";
                                ProcessBuilder builder2 = new ProcessBuilder("C:\\Windows\\System32\\cmd.exe", "/c", command2);
                                builder.redirectErrorStream(true);
                                Process p2 = builder2.start();
                                BufferedReader r2 = new BufferedReader(new InputStreamReader(p2.getInputStream()));
                                p2.waitFor();
                                String output2 = r2.readLine();
                                String[] output_trimmed = output2.trim().split("\\s+");
                                int count2 = 0;
                                long free_space = 0;
                                long total_space = 0;
                                for (String output_single : output_trimmed) {
                                    if (count2 == 1) {
                                        free_space = Long.parseLong(output_single) / 1073741824;
                                    } else if (count2 == 2) {
                                        total_space = Long.parseLong(output_single) / 1073741824;
                                    }
                                    count2++;
                                }
                                detail += '\t' + "Used Space: " + (total_space - free_space) + "GB";
                                detail += '\t' + "Free Space: " + free_space + "GB";
                                detail += '\t' + "Total Space: " + total_space + "GB";
                                search = false;
                            } else if (detail.contains("ERROR")) {
                                detail = r.readLine();
                                detail = r.readLine();
                                search = false;
                            }
                        }
                    } else {
                        detail = "PC offline or invalid hostname";
                    }
                    details_list.add(detail);
                    count++;
                    publish('\n' + "Processed " + count + "/" + pc_list.size());
                } catch (Exception e) {
                    throw new IOException();
                }
            }
            publish('\n' + "Query completed");
            return details_list;
        }
    }

    public String generateFilenameUsingDate(String prefix) {
        TimeZone timezone = TimeZone.getTimeZone("UTC");
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat filename_utc = new SimpleDateFormat("yyMMddHHmmss");
        String current_date = filename_utc.format(calendar.getTime());
        return prefix + "_" + current_date + ".txt";
    }

    public void searchButton() {
        button_search.setText("Stop");
        button_search.setActionCommand("Stop");
        button_reset.setEnabled(false);
        button_export.setEnabled(false);
        search_query.setEditable(false);
        (search_thread = new Search()).execute();
    }

    public void stopButton() {
        button_search.setText("Search");
        button_search.setActionCommand("Search");
        button_reset.setEnabled(true);
        button_export.setEnabled(true);
        search_query.setEditable(true);
        writeToSystemConsole('\n' + "Query cancelled");
        search_thread.cancel(true);
        search_thread = null;
    }

    public void resetButton() {
        search_query.setText("Enter hostnames/IP's here");
        search_results_display.setText("Search Results");
        system_console_display.setText("System Console");
    }

    public void exportButton() {
        button_search.setEnabled(false);
        button_reset.setEnabled(false);
        button_export.setEnabled(false);
        search_query.setEditable(false);
        (export_thread = new Export()).execute();
    }

    public void exitButton() {
        System.exit(0);
    }

    public void actionPerformed(ActionEvent e) {
        if ("Search" == e.getActionCommand()) {
            searchButton();
        } else if ("Stop" == e.getActionCommand()) {
            stopButton();
        } else if ("Reset" == e.getActionCommand()) {
            resetButton();
        } else if ("Export" == e.getActionCommand()) {
            exportButton();
        } else if ("Exit" == e.getActionCommand()) {
            exitButton();
        }
    }

    public Vector<String> readSearchQuery() {
        Vector<String> query_list = new Vector<String>();
        String str_query = search_query.getText();
        String line = "";
        int count = 0;
        for (char ch : str_query.toCharArray()) {
            count++;
            if (ch == '\n') {
                query_list.add(line);
                line = "";
            } else if (count == str_query.length()) {
                line += ch;
                query_list.add(line);
            } else {
                line += ch;
            }
        }
        return query_list;
    }

    public void writeToResultsDisplay(String text) {
        search_results_display.setText(text);
    }

    public void writeToResultsDisplay(Vector<String> text) {
        String results = "";
        int count = 0;
        for (String line : text) {
            count++;
            results += line;
            if (count < text.size()) {
                results += '\n';
            }
        }
        writeToResultsDisplay(results);
    }

    public void writeToSystemConsole(String text) {
        writeToSystemConsole(text, false);
    }

    public void writeToSystemConsole(String text, boolean overwrite) {
        if (overwrite) {
            system_console_display.setText(text);
        } else {
            system_console_display.setText(system_console_display.getText() + text);
        }
    }

    public void writeToSystemConsole(Vector<String> text) {
        writeToSystemConsole(text, false);
    }

    public void writeToSystemConsole(Vector<String> text, boolean overwrite) {
        String results = "";
        int count = 0;
        for (String line : text) {
            count++;
            results += line;
            if (count < text.size()) {
                results += '\n';
            }
        }
        if (overwrite) {
            writeToSystemConsole(results, true);
        } else {
            writeToSystemConsole(results, false);
        }
    }

    public Vector<String> compileDetails(Vector<String> column_one, Vector<String> column_two, String delimiter) {
        Vector<String> compiled_details = new Vector<String>();

        int count = 0;
        for (String line : column_one) {
            compiled_details.add(line + delimiter + column_two.get(count));
            count++;
        }

        return compiled_details;
    }

    public boolean pingPC(String PC) throws IOException {
        boolean pc_online = false;
        try {
            String command = "ping " + PC + " -n 1";
            ProcessBuilder builder = new ProcessBuilder("C:\\Windows\\System32\\cmd.exe", "/c", command);
            builder.redirectErrorStream(true);
            Process p = builder.start();
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            p.waitFor();

            boolean search = true;
            String detail = "";
            while (search) {
                detail = r.readLine();
                if (detail == null) {
                    pc_online = false;
                    search = false;
                } else if (detail.contains("Received = 1")) {
                    pc_online = true;
                    search = false;
                }
            }
        } catch (Exception e) {
            throw new IOException();
        }
        return pc_online;
    }

    public Vector<String> readFromFile(String filename) throws IOException {
        Vector<String> write_to_vector = new Vector<String>();
        try {
            File file = new File(filename);
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                for (String line; (line = br.readLine()) != null;) {
                    write_to_vector.add(line);
                }
            }
        } catch (Exception e) {
            throw new IOException();
        }
        return write_to_vector;
    }

    public void writeToFile(String filename, Vector<String> write_to_file) throws IOException {
        try {
            int count = 0;
            File file = new File(filename);
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
            for (String string_line : write_to_file) {
                if (count > 0) {
                    writer.newLine();
                }
                writer.write(string_line);
                count++;
            }
            writer.close();
        } catch (Exception e) {
            throw new IOException();
        }
    }
}
