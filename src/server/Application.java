package server;




import com.google.gson.Gson;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.*;
import java.util.stream.Collectors;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class Application {

    List<Result> results = new ArrayList<>();
    ReadWriteLock lock;
    Lock readLock, writeLock;


    public String fileMenu(String msg) {
        Gson gson = new Gson();
        Result result = gson.fromJson(msg,Result.class);
        fileOperations operation = new fileOperations();
        String[] arr = result.toString().split(" ");

        lock = new ReentrantReadWriteLock();
        readLock = lock.readLock();
        writeLock = lock.writeLock();

        if(arr[0].equals("set")) {
            writeLock.lock();
            //uprava
            if(operation.readJson(results,arr[1]).contains("{\"response\":\"OK\"")) {
                operation.deleteJson(results,arr[1]);
            }
            results.add(result);
            operation.writeJson(results);
            writeLock.unlock();
            return "{\"response\":\"OK\"}";
        }else if(arr[0].equals("get")) {
            readLock.lock();
            String res = operation.readJson(results,arr[1]);
            readLock.unlock();
            return res;
        }else if(arr[0].equals("delete")) {
            writeLock.lock();
            if(operation.readJson(results,arr[1]).contains("{\"response\":\"OK\"")) {
                operation.deleteJson(results,arr[1]);
                String res = operation.deleteJson(results,arr[1]);
                writeLock.unlock();
                return res;
            }else {
                return "{\"response\":\"ERROR\",\"reason\":\"No such key\"}";
            }
        }else {
            return "exit";
        }
    }

    class fileOperations {

        final String fileName = "C:\\Users\\Adam\\IdeaProjects\\JSON Database\\JSON Database\\task\\src\\server\\data\\db.json";
        final Path path = Paths.get(fileName);

        public void writeJson(List<Result> cars) {
            try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                Gson gson = new Gson();
                gson.toJson(cars, writer);
            }catch(Exception e) {
                e.printStackTrace(System.out);
            }
        }

        public String deleteJson(List<Result> hoes,String key) {
            try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                Gson gson = new Gson();
                hoes = gson.fromJson(reader,
                        new TypeToken<List<Result>>(){}.getType());
                List<Result> tmp = new ArrayList<>();
                for (Result c: hoes) {
                    if(!c.getKey().equals(key)) {
                        tmp.add(c);
                    }
                }

                try (FileOutputStream fos = new FileOutputStream(fileName);
                     OutputStreamWriter isr = new OutputStreamWriter(fos,
                             StandardCharsets.UTF_8)) {
                    gson.toJson(tmp, isr);
                    results = tmp;
                    return "{\"response\":\"OK\"}";
                }

            }catch (Exception e) {
                return "{\"response\":\"ERROR\",\"reason\":\"No such key\"}";
            }
        }

        public String readJson(List<Result> cars,String key) {
            try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {

                Gson gson = new Gson();
                cars = gson.fromJson(reader,
                        new TypeToken<List<Result>>(){}.getType());

                for (Result c: cars) {
                    if(c.getKey().equals(key)) {
                        return "{\"response\":\"OK\",\"value\":\""+c.getValue()+"\"}";
                    }
                }

            }catch(Exception e ) {
                return "{\"response\":\"ERROR\",\"reason\":\"No such key\"}";
            }
            return "{\"response\":\"ERROR\",\"reason\":\"No such key\"}";
        }
    }


    class Result {

        String type;
        String key;
        String value;

        public Result(String type, String key,String value) {
            this.type = type;
            this.key = key;
            this.value = value;
        }

        public String getType() {
            return type;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return new StringBuilder().append(this.type!=null ? this.type : "").append(" ")
                    .append(this.key!=null ? this.key : "").append(" ")
                    .append(this.value!=null ? this.value : "").toString();
        }
    }


}