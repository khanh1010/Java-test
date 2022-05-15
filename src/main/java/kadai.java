import com.opencsv.CSVWriter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class kadai {

    public static HashMap<String, String> itemMap = new HashMap<>();
    public static LinkedHashMap<String, String> dateNcode = new LinkedHashMap<>();

    public static LinkedHashMap<String, String> entries = new LinkedHashMap<>();


    public static void main(String[] args) throws IOException, ParseException {

        args = new String[]{"workDir=D:\\assignment"};

        String workDir = args[0].split("=")[1];
        String currentPath = Paths.get(workDir).toAbsolutePath().toString();

        System.out.println("Current Dir : " + currentPath);

        String InputFilesDirPath = Paths.get(currentPath, "\\input\\SalesList.csv").toString();

        String OutputDirPath = Paths.get(currentPath, "\\output\\sale_report.csv").toString();

        List<String> result = new ArrayList<>();

        //Saleitem というファイルから、hashmapを作成する。
        itemFile(itemMap);

        File csvFile = new File(InputFilesDirPath);
        BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(csvFile.toPath()), StandardCharsets.UTF_8));

        //ヘーダをskipして、ファイルを読み込む
        String line;
        reader.readLine();

        while ((line = reader.readLine()) != null) {
            result.add(line);
        }

        for (String curLine : result) {
            String[] rowSt = curLine.split(",");
            String curDate = rowSt[0];
            String curLineCode = rowSt[1];
            int stPrice = Integer.parseInt(rowSt[2]);

            if (!dateNcode.containsKey(curDate)) {


                String prevDate = getLastId(dateNcode);

                //dateNcodeに追加する
                //add to dateNcode
                dateNcode.put(curDate, curLineCode);

                //前の日付が空かどうかを確認する
                //Check if the previous date is empty or not
                if (Objects.equals(prevDate, "")) {
                    int Sum = 0;
                    String firstVal = itemMap.get(curLineCode);
                    int stValue = Integer.parseInt(firstVal);
                    addEntry(Sum, curDate, curLineCode, stPrice, stValue);
                }

                //2日間の間に日付があるかどうかを確認する
                //イエスの場合、すべてを取得してentriesに挿入します

                //check if there is any dates between 2 days
                //if yes, get all and insert to entries

                else {
                    int numDate = Date(prevDate, curDate).size();
                    if (numDate >= 1) {
                        for (int i = 0; i < numDate; i++) {
                            Date da = Date(prevDate, curDate).get(i);
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                            String strDate = dateFormat.format(da);
                            entries.put(strDate, String.valueOf(0));
                        }

                        int Sum = 0;
                        String firstVal = itemMap.get(curLineCode);
                        int stValue = Integer.parseInt(firstVal);
                        addEntry(Sum, curDate, curLineCode, stPrice, stValue);

                    } else {
                        int Sum = 0;
                        String firstVal = itemMap.get(curLineCode);
                        int stValue = Integer.parseInt(firstVal);
                        addEntry(Sum, curDate, curLineCode, stPrice, stValue);
                    }

                }

            } else {
                //dateNcodeにdateが存在した場合は既存の値に次の値を追加する。
                //if the date already exists in dateNcode, plus the next value to the existing value
                String sum01 = entries.get(curDate);
                int Sum = Integer.parseInt(sum01);
                String firstVal = itemMap.get(curLineCode);
                int stValue = Integer.parseInt(firstVal);
                addEntry(Sum, curDate, curLineCode, stPrice, stValue);


            }

        }
        writeToOutput(entries, OutputDirPath);


    }

    private static void addEntry(int sum, String date, String nextLineCode, int ndPrice, int xtValue) {
        for (HashMap.Entry<String, String> set : itemMap.entrySet()) {
            if (Objects.equals(nextLineCode, set.getKey())) {
                sum = sum + (xtValue * ndPrice);
                String res2 = String.valueOf(sum);
                entries.put(date, res2);
            }
        }
    }

    private static void writeToOutput(LinkedHashMap<String, String> entries, String path) {

        File file = new File(path);

        BufferedWriter bf = null;

        try {

            bf = new BufferedWriter(new FileWriter(file));
            CSVWriter writer = new CSVWriter(bf);
            String[] header = {"販売日", "売上総額"};
            writer.writeNext(header);
            for (Map.Entry<String, String> entry :
                    entries.entrySet()) {

                // put key and value separated by a comma
                bf.write(entry.getKey() + ","
                        + entry.getValue());

                // new line
                bf.newLine();
            }

            bf.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            try {

                // always close the writer
                assert bf != null;
                bf.close();
            } catch (Exception ignored) {
            }
        }

    }


    public static void itemFile(HashMap<String, String> itemMap) {
        File itemFile = new File("D:\\assignment\\input\\SalesItem.csv");
        String line = "";
        try (BufferedReader br = new BufferedReader(new FileReader(itemFile))) {

            while ((line = br.readLine()) != null) {
                String[] item = line.split(",");
                itemMap.put(item[0], item[2]);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Get the dates between 2 days
    public static List<Date> Date(String date1, String date2) {

        List<Date> dates = new ArrayList<>();

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            Calendar cal01 = Calendar.getInstance();
            Calendar cal02 = Calendar.getInstance();
            cal01.setTime(dateFormat.parse(date1));
            cal02.setTime(dateFormat.parse(date2));
            cal01.add(Calendar.DATE, 1);
            cal02.add(Calendar.DATE, -1);
            Date date01 = cal01.getTime();
            Date date02 = cal02.getTime();
            String strDate01 = dateFormat.format(date01);
            String strDate02 = dateFormat.format(date02);

            Date startDate = formatter.parse(strDate01);
            Date endDate = formatter.parse(strDate02);
            long interval = 24 * 1000 * 60 * 60; // 1 hour in millis
            long endTime = endDate.getTime();
            long curTime = startDate.getTime();
            while (curTime <= endTime) {
                dates.add(new Date(curTime));
                curTime += interval;
            }
            return dates;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dates;
    }

    //Get the last date in dateNcode LinkedHashMap
    private static String getLastId(LinkedHashMap<String, String> dateNcode) {

        int count = 1;

        String date = "";
        for (Map.Entry<String, String> it :
                dateNcode.entrySet()) {

            if (count == dateNcode.size()) {
                date = it.getKey();
            }
            count++;
        }
        return date;
    }

}
