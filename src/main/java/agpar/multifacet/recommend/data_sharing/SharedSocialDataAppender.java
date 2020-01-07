package agpar.multifacet.recommend.data_sharing;

import com.google.common.collect.BiMap;
import net.librec.conf.Configuration;
import net.librec.data.DataAppender;
import net.librec.data.convertor.appender.SocialDataAppender;
import net.librec.math.structure.SparseMatrix;

import java.io.IOException;
import java.util.HashMap;

public class SharedSocialDataAppender extends SocialDataAppender implements DataAppender {
    private static HashMap<String, SharedSocialDataAppender> instances = new HashMap<>();
    private SocialDataAppender sda;

    private SharedSocialDataAppender(Configuration conf) {
        this.sda = new SocialDataAppender(conf);
    }

    public static synchronized SharedSocialDataAppender getInstance(Configuration conf) {
        String key = conf.get("data.appender.path");
        if(!instances.containsKey(key)) {
            instances.put(key, new SharedSocialDataAppender(conf));
        }
        return instances.get(key);
    }

    public static synchronized void reset() {
        instances.clear();
    }

    @Override
    public synchronized void processData() throws IOException {
        if (sda.getUserAppender() == null) {
            sda.processData();
        }
    }

    public SparseMatrix getUserAppender() {
        return sda.getUserAppender();
    }

    @Override
    public void setUserMappingData(BiMap<String, Integer> userMappingData) {
        sda.setUserMappingData(userMappingData);

    }

    @Override
    public void setItemMappingData(BiMap<String, Integer> itemMappingData) {
        sda.setItemMappingData(itemMappingData);
    }
}
