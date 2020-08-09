import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.TreeSet;

@Getter
@ToString(of = "url")
@RequiredArgsConstructor
public class Link implements Comparable<Link> {
    private final String url;
    private TreeSet<Link> children = new TreeSet<>();

    @Override
    public int compareTo(Link link) {
        return url.compareTo(link.url);
    }
}
