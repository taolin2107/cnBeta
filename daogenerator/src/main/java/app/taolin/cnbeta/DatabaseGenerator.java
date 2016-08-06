package app.taolin.cnbeta;

import org.greenrobot.greendao.generator.DaoGenerator;
import org.greenrobot.greendao.generator.Entity;
import org.greenrobot.greendao.generator.Schema;

public class DatabaseGenerator {

    public static void main(String[] args) {
        Schema schema = new Schema(2, "app.taolin.cnbeta.dao");
        addListItem(schema);
        addArticle(schema);
        try {
            new DaoGenerator().generateAll(schema, "app/src/main/java");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void addListItem(final Schema schema) {
        Entity listItem = schema.addEntity("ListItem");
        listItem.setHasKeepSections(true);
        listItem.addStringProperty("sid").primaryKey();
        listItem.addStringProperty("title").notNull();
        listItem.addStringProperty("pubtime").notNull();
        listItem.addBooleanProperty("isread").notNull();
        listItem.addBooleanProperty("isfavor").notNull();
        listItem.addBooleanProperty("isheadline").notNull();
        listItem.addStringProperty("collecttime");
        listItem.addStringProperty("thumb");
        listItem.addIntProperty("headindex");
    }

    private static void addArticle(final Schema schema) {
        Entity article = schema.addEntity("Article");
        article.setHasKeepSections(true);
        article.addStringProperty("sid").primaryKey();
        article.addStringProperty("title").notNull();
        article.addStringProperty("time").notNull();
        article.addStringProperty("source").notNull();
        article.addStringProperty("counter").notNull();
        article.addStringProperty("good").notNull();
        article.addStringProperty("comments").notNull();
        article.addStringProperty("hometext").notNull();
        article.addStringProperty("bodytext").notNull();
    }
}
