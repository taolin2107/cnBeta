package app.taolin.cnbeta;

import org.greenrobot.greendao.generator.DaoGenerator;
import org.greenrobot.greendao.generator.Entity;
import org.greenrobot.greendao.generator.Schema;

public class DatabaseGenerator {

    public static void main(String[] args) {
        Schema schema = new Schema(1, "app.taolin.cnbeta.dao");
        addFavorItem(schema);
        addListItem(schema);
        addArticle(schema);
        addHeadline(schema);
        try {
            new DaoGenerator().generateAll(schema, "app/src/main/java");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void addFavorItem(final Schema schema) {
        Entity listItem = schema.addEntity("FavorItem");
        listItem.setHasKeepSections(true);
        listItem.addStringProperty("sid").primaryKey();
        listItem.addStringProperty("title").notNull();
        listItem.addStringProperty("pubtime");
        listItem.addStringProperty("collecttime");
    }

    private static void addListItem(final Schema schema) {
        Entity listItem = schema.addEntity("ListItem");
        listItem.setHasKeepSections(true);
        listItem.addStringProperty("sid").primaryKey();
        listItem.addStringProperty("title").notNull();
        listItem.addStringProperty("pubtime");
        listItem.addBooleanProperty("isread").notNull();
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

    private static void addHeadline(final Schema schema) {
        Entity listItem = schema.addEntity("Headline");
        listItem.setHasKeepSections(true);
        listItem.addStringProperty("sid").primaryKey();
        listItem.addStringProperty("title").notNull();
        listItem.addStringProperty("thumb").notNull();
        listItem.addIntProperty("index").notNull();
    }
}
