import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.RecursiveAction;

@RequiredArgsConstructor
public class TreeTraversal extends RecursiveAction {
    private static final Logger errorLogger = LogManager.getLogger("errorLogger");
    private final Link link;

    @Override
    protected void compute() {
        // Блокируем обход ссылок, ведущие на документы (например, PDF)
        int indexFirstPoint = link.getUrl().indexOf(".");
        int indexAnotherPoint = link.getUrl().indexOf(".", indexFirstPoint + 1);
        if (indexAnotherPoint != -1) return;

        TreeSet<Link> children = link.getChildren();
        List<TreeTraversal> subTasks = new ArrayList<>();

        // Задержка запросов (500-999 ms), чтобы не получить блокировку на сайте
        try {
            Thread.sleep((int) (Math.random() * 500) + 500);
        } catch (InterruptedException e) {
            e.printStackTrace();
            errorLogger.error(e.getMessage(), e);
            return;
        }

        Document doc;
        try {
            doc = Jsoup.connect(link.getUrl()).maxBodySize(0).get();
        } catch (IOException e) {
            errorLogger.error(e.getMessage(), e);
            return;
        }
        Elements links = doc.select("body").select("[href]");

        for (Element currentLink : links) {
            String url = currentLink.attr("abs:href");

            if (url.length() == (link.getUrl().length() + 1)) continue; // Пропускаем дубликаты
            if (url.contains("#")) continue;                            // Пропускаем отдельные ссылки на участки текущей страницы

            Link tmp = new Link(url);
            // Избегаем повторяющихся ссылок на текущей странице
            if (!children.contains(tmp)) {
                // Если начало адреса такое же, как у родителя, И длина больше родителя
                if ((url.startsWith(this.link.getUrl())) && (url.length() > this.link.getUrl().length())) {
                    Link newLink = new Link(url);
                    children.add(newLink);

                    TreeTraversal task = new TreeTraversal(newLink);
                    task.fork();                    // Асинхронно запускаем проход по ссылкам текущей страницы
                    subTasks.add(task);
                }
            }
        }

        // Дожидаемся выполнения задачи
        for (TreeTraversal task : subTasks) {
            task.join();
        }
    }
}
