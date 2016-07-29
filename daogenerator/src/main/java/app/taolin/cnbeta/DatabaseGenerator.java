package app.taolin.cnbeta;

import org.greenrobot.greendao.generator.DaoGenerator;
import org.greenrobot.greendao.generator.Entity;
import org.greenrobot.greendao.generator.Schema;

public class DatabaseGenerator {

    public static void main(String[] args) {
        Schema schema = new Schema(1, "app.taolin.cnbeta.dao");
        addFavorArticle(schema);
        try {
            new DaoGenerator().generateAll(schema, "app/src/main/java");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void addFavorArticle(final Schema schema) {
        Entity favorArticle = schema.addEntity("FavorArticle");
        favorArticle.setHasKeepSections(true);
        favorArticle.addStringProperty("sid").primaryKey();
        favorArticle.addStringProperty("title").notNull();
        favorArticle.addStringProperty("pubtime").notNull();
        favorArticle.addStringProperty("collecttime").notNull();
        favorArticle.addStringProperty("source");
        favorArticle.addStringProperty("counter").notNull();
        favorArticle.addStringProperty("good");
        favorArticle.addStringProperty("comments").notNull();
        favorArticle.addStringProperty("hometext");
        favorArticle.addStringProperty("bodytext");
    }
}
