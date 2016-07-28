package app.taolin.cnbeta.models;

import java.util.List;

/**
 * @author taolin
 * @version v1.0
 * @date Jul 11, 2016.
 * @description
 */

public class ContentList {

    public String status;
    public List<Result> result;

    public class Result {
        public String sid;
        public String title;
        public String pubtime;
        public String summary;
        public String topic;
        public String counter;
        public String comments;
        public String ratings;
        public String score;
        public String ratings_story;
        public String score_story;
        public String topic_logo;
        public String thumb;
        public boolean is_read = false;

        @Override
        public boolean equals(Object o) {
            return o instanceof Result && sid.equals(((Result) o).sid);
        }

        @Override
        public int hashCode() {
            return Integer.parseInt(sid.trim());
        }
    }
}
