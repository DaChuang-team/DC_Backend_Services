package org.dachuang_team.dc_backend_services.services;

import org.dachuang_team.dc_backend_services.pojo.Attraction;
import org.dachuang_team.dc_backend_services.pojo.SysImage;
import org.dachuang_team.dc_backend_services.repository.AttractionRepository;
import org.dachuang_team.dc_backend_services.repository.SysImageRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@Order(2) // Run after DataConsistencyRunner
public class DataSeeder implements ApplicationRunner {

    private final AttractionRepository attractionRepository;
    private final Random random = new Random();
    private final SysImageRepository sysImageRepository;

    public DataSeeder(AttractionRepository attractionRepository, SysImageRepository sysImageRepository) {
        this.attractionRepository = attractionRepository;
        this.sysImageRepository = sysImageRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // Generate image data
        long imageCount = sysImageRepository.count();
        if (imageCount == 0) {
            List<SysImage> SysImages = new ArrayList<>();
            SysImages.add(createImage("https://dc-img-test.oss-cn-guangzhou.aliyuncs.com/1.jpg", "1.jpg", "MAIN_PAGE_BANNER"));
            SysImages.add(createImage("https://dc-img-test.oss-cn-guangzhou.aliyuncs.com/2.png", "2.png", "MAIN_PAGE_BANNER"));
            SysImages.add(createImage("https://dc-img-test.oss-cn-guangzhou.aliyuncs.com/3.jpg", "3.jpg", "MAIN_PAGE_BANNER"));
            sysImageRepository.saveAll(SysImages);
            System.out.println("Generated 3 images.");
        }

        long count = attractionRepository.count();
        if (count > 0) {
            return; // Data already exists
        }

        List<Attraction> attractions = new ArrayList<>();

        // 生成20个广州的乡村/自然景点
        attractions.add(createAttraction("石门国家森林公园", "位于广州市从化区，被称为\"广州香山\"，拥有红叶、花海等四季美景。", 23.65, 113.80, "自然", "广州"));
        attractions.add(createAttraction("流溪河国家森林公园", "位于从化区，以其清澈的流溪河和美丽的山水风光著称。", 23.72, 113.90, "自然", "广州"));
        attractions.add(createAttraction("白水寨风景名胜区", "位于增城区，拥有全国内地落差最大的瀑布——白水仙瀑。", 23.58, 113.82, "自然", "广州"));
        attractions.add(createAttraction("溪头村", "位于从化区良口镇，被誉为\"广东省最美乡村\"，古朴宁静。", 23.70, 113.95, "乡村", "广州"));
        attractions.add(createAttraction("红山村", "位于花都区梯面镇，以油菜花海和乡村风光闻名。", 23.50, 113.30, "乡村", "广州"));
        attractions.add(createAttraction("宝趣玫瑰世界", "位于从化区，是一个以玫瑰文化为特色的主题公园。", 23.55, 113.55, "主题公园", "广州"));
        attractions.add(createAttraction("南沙湿地公园", "位于广州最南端，是候鸟迁徙的重要驿站，生态环境优美。", 22.60, 113.65, "湿地", "广州"));
        attractions.add(createAttraction("百万葵园", "位于南沙区，是中国首个以向日葵为主题的公园。", 22.65, 113.60, "主题公园", "广州"));
        attractions.add(createAttraction("大夫山森林公园", "位于番禺区，拥有茂密的森林和清新的空气，适合骑行和徒步。", 22.95, 113.30, "公园", "广州"));
        attractions.add(createAttraction("莲花山旅游区", "位于番禺区，集古采石场遗址和佛教文化于一体。", 22.98, 113.50, "文化", "广州"));
        attractions.add(createAttraction("沙湾古镇", "位于番禺区，拥有800多年历史，保留了大量明清古建筑。", 22.90, 113.35, "历史", "广州"));
        attractions.add(createAttraction("余荫山房", "位于番禺区，广东四大名园之一，精致典雅。", 23.02, 113.40, "历史", "广州"));
        attractions.add(createAttraction("宝墨园", "位于番禺区，集清宫文化、岭南古建筑、岭南园林艺术于一体。", 22.88, 113.28, "文化", "广州"));
        attractions.add(createAttraction("黄埔古港", "位于海珠区，见证了广州\"海上丝绸之路\"的繁荣。", 23.08, 113.40, "历史", "广州"));
        attractions.add(createAttraction("小洲村", "位于海珠区，保留了岭南水乡的传统风貌，艺术氛围浓厚。", 23.05, 113.35, "乡村", "广州"));
        attractions.add(createAttraction("帽峰山森林公园", "位于白云区，是广州市区的最高峰之一，自然生态良好。", 23.28, 113.45, "自然", "广州"));
        attractions.add(createAttraction("火炉山森林公园", "位于天河区，因山上泥土多为红泥土而得名，适合登山。", 23.18, 113.40, "公园", "广州"));
        attractions.add(createAttraction("从化温泉风景区", "位于从化区，拥有珍稀的氡温泉，素有\"岭南第一泉\"的美誉。", 23.63, 113.63, "温泉", "广州"));
        attractions.add(createAttraction("增城挂绿广场", "位于增城区，因拥有一棵著名的挂绿荔枝树而闻名。", 23.28, 113.82, "文化", "广州"));
        attractions.add(createAttraction("1978电影小镇", "位于增城区，由旧糖厂改造而成的文化创意产业园。", 23.27, 113.83, "文化", "广州"));

        // 生成20个广州的酒店（专注于乡村/度假村/郊区）
        attractions.add(createHotel("碧水湾温泉度假村", "位于从化流溪河畔，提供高品质温泉服务和豪华住宿。", 23.68, 113.65, "酒店", "广州"));
        attractions.add(createHotel("都喜泰丽温泉度假酒店", "位于从化区，泰式风情与岭南温泉的完美结合。", 23.62, 113.68, "酒店", "广州"));
        attractions.add(createHotel("从都国际庄园", "位于从化区，高端私密庄园，提供顶级管家服务。", 23.66, 113.70, "酒店", "广州"));
        attractions.add(createHotel("三英温泉度假酒店", "位于增城白水寨风景区，拥有独特的洞穴温泉。", 23.56, 113.80, "酒店", "广州"));
        attractions.add(createHotel("合汇温泉酒店", "位于增城区，融合现代设计与自然景观。", 23.54, 113.81, "酒店", "广州"));
        attractions.add(createHotel("慕思睡眠酒店", "位于增城区，专注于提供优质睡眠体验。", 23.25, 113.82, "酒店", "广州"));
        attractions.add(createHotel("广州花都皇冠假日酒店", "位于花都区，临近广州白云国际机场，交通便利。", 23.40, 113.25, "酒店", "广州"));
        attractions.add(createHotel("广州花都木莲庄酒店", "位于花都区，设计典雅，环境清幽。", 23.45, 113.28, "酒店", "广州"));
        attractions.add(createHotel("广州长隆酒店", "位于番禺长隆旅游度假区，生态主题酒店。", 22.99, 113.32, "酒店", "广州"));
        attractions.add(createHotel("长隆熊猫酒店", "位于番禺区，以熊猫三胞胎为主题，适合亲子入住。", 22.99, 113.33, "酒店", "广州"));
        attractions.add(createHotel("广州卓美亚酒店", "位于天河区珠江新城，尽享城市繁华与江景。", 23.12, 113.32, "酒店", "广州"));
        attractions.add(createHotel("广州瑰丽酒店", "位于周大福金融中心，拥有壮丽的城市天际线景观。", 23.11, 113.32, "酒店", "广州"));
        attractions.add(createHotel("白天鹅宾馆", "位于荔湾区沙面岛，中国第一家中外合作的五星级宾馆。", 23.10, 113.24, "酒店", "广州"));
        attractions.add(createHotel("广州花园酒店", "位于越秀区，岭南文化与现代奢华的典范。", 23.13, 113.28, "酒店", "广州"));
        attractions.add(createHotel("中国大酒店", "位于越秀区，历史悠久，服务卓越。", 23.14, 113.25, "酒店", "广州"));
        attractions.add(createHotel("东方宾馆", "位于越秀区，繁华都市中的园林式宾馆。", 23.14, 113.26, "酒店", "广州"));
        attractions.add(createHotel("广州富力丽思卡尔顿酒店", "位于天河区，奢华典雅，紧邻广东博物馆。", 23.11, 113.31, "酒店", "广州"));
        attractions.add(createHotel("广州香格里拉大酒店", "位于海珠区，毗邻广交会展馆，拥有美丽的花园。", 23.10, 113.36, "酒店", "广州"));
        attractions.add(createHotel("广州南沙大酒店", "位于南沙区，背山面海，景色宜人。", 22.78, 113.60, "酒店", "广州"));
        attractions.add(createHotel("广州翡翠希尔顿酒店", "位于黄埔区科学城，环境优美，设施完善。", 23.16, 113.45, "酒店", "广州"));

        attractionRepository.saveAll(attractions);
        System.out.println("Generated 20 hotels and 20 attractions.");

    }

    private Attraction createAttraction(String name, String description, double lat, double lon, String tag, String region) {
        Attraction a = new Attraction();
        a.setAttractionName(name);
        a.setAttractionDescription(description);
        a.setLatitude(lat);
        a.setLongitude(lon);
        a.setAttractionTag(tag); // Tag for type (Nature, Village, etc.)
        a.setAttractionRegion(region);
        a.setAttractionRating(3.5 + random.nextDouble() * 1.5); // Rating 3.5 - 5.0
        a.setAttractionImageURL("https://example.com/images/" + name.hashCode() + ".jpg"); // Placeholder
        return a;
    }

    private Attraction createHotel(String name, String description, double lat, double lon, String tag, String region) {
        Attraction h = new Attraction();
        h.setAttractionName(name);
        h.setAttractionDescription(description);
        h.setLatitude(lat);
        h.setLongitude(lon);
        h.setAttractionTag(tag); // 标签 "酒店" 用于区分
        h.setAttractionRegion(region);
        h.setAttractionRating(4.0 + random.nextDouble() * 1.0); // 评分 4.0 - 5.0
        h.setAttractionImageURL("https://example.com/hotels/" + name.hashCode() + ".jpg"); // 占位符
        return h;
    }

    private SysImage createImage(String url, String name, String purpose) {
        SysImage img = new SysImage();
        img.setImageUrl(url);
        img.setImageName(name);
        img.setPurpose(purpose);
        return img;
    }
}