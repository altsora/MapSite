import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    private static final String ROOT_ADDRESS = "https://skillbox.ru";
    private static final Link rootLink = new Link(ROOT_ADDRESS);
    private static final String FILE_PATH = "result/siteMap.txt";

    private static final Logger rootLogger = LogManager.getRootLogger();
    private static final Logger errorLogger = LogManager.getLogger("errorLogger");

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        rootLogger.info("Начало работы!");
        ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
        Runnable task = () -> rootLogger.info("Формирование карты сайта...");
        service.scheduleWithFixedDelay(task, 10, 10, TimeUnit.SECONDS);  // Выдавать сообщение об обходе раз в 10 секунд
        new ForkJoinPool().invoke(new TreeTraversal(rootLink));     // Запуск рекурсивного многопоточного обхода
        service.shutdown();                                         // Остановка выдачи сообщений об обходе
        long end = System.currentTimeMillis() - start;
        rootLogger.info("Формирование карты сайта завершено. Время работы: {} сек.",(end / 1000.));
        writeMapInFile();
        rootLogger.info("Запись в файл завершена.");
        rootLogger.info("Общее время работы: {} сек.", ((System.currentTimeMillis() - start) / 1000.));
    }

    public static void walkOnTree(Set<Link> children, int countTab, BufferedWriter bw) throws IOException {
        if (countTab == 1) bw.write(ROOT_ADDRESS + "\n");
        if (children.isEmpty()) return;
        for (Link child : children) {
            bw.write("\t".repeat(countTab) + child.getUrl() + "\n");
            walkOnTree(child.getChildren(), countTab + 1, bw);
        }
    }

    public static void writeMapInFile() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH))) {
            walkOnTree(rootLink.getChildren(), 1, bw);
        } catch (IOException ex) {
            ex.printStackTrace();
            errorLogger.error(ex.getMessage(), ex);
        }
    }
}
