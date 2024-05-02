package gb.data;

import java.util.List;
import java.util.stream.Collectors;

public class PostData {
    private List<String> articles;
    private List<String> variants;
    private String fileExtension;
    private String title;
    private String description;
    private String getHtmlStartSearch;
    private String getHtmlStartSearchOffset;
    private String getHtmlEndSearch;
    private String getHtmlEndSearchOffset;

    public PostData(Project project, String fileExtension ) {
        this.title = project.getTitle();
        this.description = project.getDescription();
        this.articles = project.getArticles().stream().map(Article::getArticle_content).collect(Collectors.toList());
        this.variants = project.getVariants().stream().map(Variants::getVariant_content).collect(Collectors.toList());
        this.fileExtension = fileExtension;
        this.getHtmlStartSearch = project.getJsonData("getHtmlStartSearch");
        this.getHtmlStartSearchOffset = project.getJsonData("getHtmlStartSearchOffset");
        this.getHtmlEndSearch = project.getJsonData("getHtmlEndSearch");
        this.getHtmlEndSearchOffset = project.getJsonData("getHtmlEndSearchOffset");
    }


}
