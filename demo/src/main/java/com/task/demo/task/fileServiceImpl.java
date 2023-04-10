package com.task.demo.task;

import com.task.demo.task.model.Pair;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class fileServiceImpl implements fileService {
    private static final int A_DATE_FROM = 0;
    private static final int A_DATE_TO = 1;
    private static final int B_DATE_FROM = 2;
    private static final int B_DATE_TO = 3;
    private static final int FIRST_DATE_FROM = 2;
    private static final int FIRST_DATE_TO = 3;
    private static final int SECOND_DATE_FROM = 2;
    private static final int SECOND_DATE_TO = 3;
    private static final int A_EMPL_ID = 0;
    private static final int B_EMPL_ID = 0;
    private static final int A_PROJECT_ID = 1;
    private static final int B_PROJECT_ID = 1;
    private List<String[]> records;


    private List<String[]> readFile(MultipartFile multipartFile) {
        try {
            List<String[]> records = new ArrayList<>();
            File file = convert(multipartFile);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            while (reader.ready()) {
                String[] record = reader.readLine().split(", ");
                records.add(record);
            }
            this.records = records;
            return records;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private File convert(MultipartFile file) {
        File convFile = new File(file.getOriginalFilename());
        try {
            convFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(convFile);
            fos.write(file.getBytes());
            fos.close();
        } catch (IOException e) {
            convFile = null;
        }

        return convFile;
    }

    public Map<String[], Long> findAllSelectedPairs(Pair pair) {
        try {
            List<String[]> allRecords = this.records;
            Map<String[], Long> pairs = new HashMap<>();
            for (int i = 0; i < allRecords.size() - 1; i++) {
                for (int j = i + 1; j < allRecords.size(); j++) {
                    String[] firstRec = allRecords.get(i);
                    String[] secondRec = allRecords.get(j);

                    if (Objects.equals(firstRec[A_PROJECT_ID], secondRec[B_PROJECT_ID])
                            && hasOverlap(firstRec, secondRec)) {
                        long overlapDays = calculateOverlap(firstRec, secondRec);

                        if (overlapDays > 0) {
                            updatePairsMap(pairs, firstRec, secondRec, overlapDays);
                        }
                    }
                }
            }
            Map<String[], Long> selectedPairs = new HashMap<>();
            for (Map.Entry<String[], Long> entry : pairs.entrySet()) {
                if (entry.getKey()[0].equalsIgnoreCase(pair.getFirstEmpl()) &&
                        entry.getKey()[1].equalsIgnoreCase(pair.getSecondEmpl()) ||
                        entry.getKey()[1].equalsIgnoreCase(pair.getFirstEmpl()) &&
                                entry.getKey()[0].equalsIgnoreCase(pair.getSecondEmpl())) {
                    selectedPairs.put(entry.getKey(), entry.getValue());
                }
            }

            return selectedPairs;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Set<String> getAllEmplIds(MultipartFile file) {
        this.records = null;
        List<String[]> allRecords = readFile(file);
        Set<String> empl = new HashSet<>();
        for (String[] s :
                allRecords) {
            empl.add(s[0]);
        }
        return empl;
    }

    private boolean hasOverlap(String[] firstRec, String[] secondRec) {
        List<LocalDate> dates = parseDate(firstRec, secondRec);

        return (dates.get(A_DATE_FROM).isBefore(dates.get(B_DATE_TO))
                || dates.get(A_DATE_FROM).isEqual(dates.get(B_DATE_TO)))
                && (dates.get(A_DATE_TO).isAfter(dates.get(B_DATE_FROM))
                || dates.get(A_DATE_TO).isEqual(dates.get(B_DATE_TO)));
    }

    private List<LocalDate> parseDate(String[] firstRec, String[] secondRec) {
        List<LocalDate> parsedDates = new ArrayList<>();
        List<SimpleDateFormat>formats=new ArrayList<>();
        SimpleDateFormat formatA = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat formatB = new SimpleDateFormat("dd-MM-yyyy");
//        SimpleDateFormat formatC = new SimpleDateFormat("MM-dd-yyyy");
        formats.add(formatA);
        formats.add(formatB);
//        formats.add(formatC);
        try {
            Date date=null;
            for(int i =0;i<formats.size();i++)
            {
                date = formats.get(i).parse(firstRec[FIRST_DATE_FROM]);
                if(date.getYear()+1900>2000)
                {break;}
            }
            LocalDate aFrom = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            aFrom.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            parsedDates.add(LocalDate.from(aFrom));
        } catch (Exception e) {
            LocalDate aFrom = LocalDate.parse(firstRec[FIRST_DATE_FROM]);
            aFrom.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            parsedDates.add(aFrom);
        }
        if (!firstRec[3].equalsIgnoreCase("null")) {
            try {
                Date date=null;
                for(int i =0;i<formats.size();i++)
                {
                    date = formats.get(i).parse(firstRec[FIRST_DATE_TO]);
                    long year=date.getYear()+1900;
                    if(date.getYear()+1900>2000)
                    {break;}
                }
                LocalDate aTo = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                aTo.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                parsedDates.add(LocalDate.from(aTo));
            } catch (Exception e) {
                LocalDate aTo = LocalDate.parse(firstRec[FIRST_DATE_TO]);
                aTo.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                parsedDates.add(aTo);
            }
        } else {
            parsedDates.add(LocalDate.now());
        }
        try {
            Date date=null;
            for(int i =0;i<formats.size();i++)
            {
                date = formats.get(i).parse(secondRec[SECOND_DATE_FROM]);
                if(date.getYear()+1900>2000)
                {break;}
            }
            LocalDate bFrom = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            bFrom.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            parsedDates.add(LocalDate.from(bFrom));
        } catch (Exception e) {
            LocalDate bFrom = LocalDate.parse(secondRec[SECOND_DATE_FROM]);
            bFrom.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            parsedDates.add(bFrom);
        }
        if (!secondRec[3].equalsIgnoreCase("null")) {
            try {
                Date date=null;
                for(int i =0;i<formats.size();i++)
                {
                    date = formats.get(i).parse(secondRec[SECOND_DATE_TO]);
                    if(date.getYear()+1900>2000)
                    {break;}
                }
                LocalDate bTo = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                bTo.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                parsedDates.add(LocalDate.from(bTo));
            } catch (Exception e) {
                LocalDate bFrom = LocalDate.parse(secondRec[SECOND_DATE_TO]);
                bFrom.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                parsedDates.add(bFrom);
            }
        } else {
            parsedDates.add(LocalDate.now());
        }
        return parsedDates;
    }

    private long calculateOverlap(String[] firstRec, String[] secondRec) {
        List<LocalDate> dates = parseDate(firstRec, secondRec);

        LocalDate periodStartDate =
                dates.get(A_DATE_FROM).isBefore(dates.get(B_DATE_FROM)) ?
                        dates.get(B_DATE_FROM) : dates.get(A_DATE_FROM);

        LocalDate periodEndDate =
                dates.get(A_DATE_TO).isBefore(dates.get(B_DATE_TO)) ?
                        dates.get(A_DATE_TO) : dates.get(B_DATE_TO);

        return Math.abs(ChronoUnit.DAYS.between(periodStartDate, periodEndDate));
    }

    private void updatePairsMap(Map<String[], Long> pairs, String[] firstRec, String[] secondRec, long overlapDays) {
        AtomicBoolean isPresent = new AtomicBoolean(false);
        String aEmplId = firstRec[A_EMPL_ID];
        String bEmplId = secondRec[B_EMPL_ID];
        for (Map.Entry<String[], Long> pair : pairs.entrySet()) {
            if (isPairPresent(pair, aEmplId, bEmplId, firstRec[1])) {

                pair.setValue(overlapDays);
                isPresent.set(true);
            }
        }
        if (!isPresent.get()) {
            String[] newpair = {aEmplId, bEmplId, firstRec[1]};
            pairs.put(newpair, overlapDays);
        }
    }

    private boolean isPairPresent(Map.Entry<String[], Long> pair, String aEmplId, String bEmplId, String projectId) {
        String[] pairIds = pair.getKey();
        return ((Objects.equals(pairIds[0], aEmplId)
                && Objects.equals(pairIds[1], bEmplId)) && Objects.equals(pairIds[2], projectId)
                || ((Objects.equals(pairIds[0], bEmplId)
                && Objects.equals(pairIds[1], aEmplId)) && Objects.equals(pairIds[2], projectId)));
    }

}
