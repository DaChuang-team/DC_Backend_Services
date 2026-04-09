package org.dachuang_team.dc_backend_services.services;

import org.dachuang_team.dc_backend_services.domain.PO.HotelHomestay;
import org.dachuang_team.dc_backend_services.domain.PO.ProductPO.Product;
import org.dachuang_team.dc_backend_services.repository.HotelHomestayRepository;
import org.dachuang_team.dc_backend_services.repository.ProductRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@Order(3)
public class HotelAndProductSeeder implements ApplicationRunner {
    private final HotelHomestayRepository hotelHomestayRepository;
    private final ProductRepository productRepository;

    public HotelAndProductSeeder(HotelHomestayRepository hotelHomestayRepository, ProductRepository productRepository) {
        this.hotelHomestayRepository = hotelHomestayRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (hotelHomestayRepository.count() == 0) {
            List<HotelHomestay> items = new ArrayList<>();
            items.add(makeHotel("溪田山房", "广州市从化区良口镇溪头村", 23.70, 113.95, 888.0, "https://example.com/images/hotel1.jpg", "13800138001", true));
            items.add(makeHotel("静水莲山", "广州市增城区派潭镇白水寨大道", 23.58, 113.82, 1280.0, "https://example.com/images/hotel2.jpg", "13800138002", true));
            items.add(makeHotel("稻香里", "广州市花都区梯面镇红山村", 23.50, 113.30, 680.0, "https://example.com/images/hotel3.jpg", "13800138003", false));
            items.add(makeHotel("山涧别院", "广州市从化区温泉镇温泉东路", 23.63, 113.63, 1580.0, "https://example.com/images/hotel4.jpg", "13800138004", true));
            items.add(makeHotel("田缘花舍", "广州市南沙区万顷沙镇百万葵园", 22.65, 113.60, 750.0, "https://example.com/images/hotel5.jpg", "13800138005", true));
            items.add(makeHotel("古港客栈", "广州市海珠区琶洲街黄埔古港", 23.08, 113.40, 450.0, "https://example.com/images/hotel6.jpg", "13800138006", true));
            items.add(makeHotel("岭南小院", "广州市番禺区沙湾镇沙湾古镇", 22.90, 113.35, 520.0, "https://example.com/images/hotel7.jpg", "13800138007", false));
            items.add(makeHotel("花田喜宿", "广州市花都区赤坭镇", 23.45, 113.15, 600.0, "https://example.com/images/hotel8.jpg", "13800138008", true));
            items.add(makeHotel("温泉雅居", "广州市从化区温泉镇", 23.64, 113.62, 980.0, "https://example.com/images/hotel9.jpg", "13800138009", true));
            items.add(makeHotel("水乡人家", "广州市南沙区东涌镇", 22.85, 113.45, 480.0, "https://example.com/images/hotel10.jpg", "13800138010", true));
            items.add(makeHotel("竹林深处", "广州市增城区正果镇", 23.40, 113.90, 780.0, "https://example.com/images/hotel11.jpg", "13800138011", true));
            items.add(makeHotel("山景小筑", "广州市白云区帽峰山", 23.28, 113.45, 550.0, "https://example.com/images/hotel12.jpg", "13800138012", true));
            items.add(makeHotel("湖畔人家", "广州市花都区芙蓉度假区", 23.52, 113.22, 850.0, "https://example.com/images/hotel13.jpg", "13800138013", false));
            items.add(makeHotel("艺术客栈", "广州市海珠区小洲村", 23.05, 113.35, 420.0, "https://example.com/images/hotel14.jpg", "13800138014", true));
            items.add(makeHotel("森林木屋", "广州市从化区石门国家森林公园", 23.65, 113.80, 1100.0, "https://example.com/images/hotel15.jpg", "13800138015", true));
            items.add(makeHotel("江景美宿", "广州市荔湾区沙面", 23.10, 113.24, 1350.0, "https://example.com/images/hotel16.jpg", "13800138016", true));
            items.add(makeHotel("田园牧歌", "广州市白云区钟落潭镇", 23.38, 113.40, 620.0, "https://example.com/images/hotel17.jpg", "13800138017", true));
            items.add(makeHotel("星空帐篷", "广州市增城区派潭镇", 23.57, 113.81, 950.0, "https://example.com/images/hotel18.jpg", "13800138018", true));
            items.add(makeHotel("荔枝庭院", "广州市增城区增江街", 23.28, 113.82, 580.0, "https://example.com/images/hotel19.jpg", "13800138019", false));
            items.add(makeHotel("花海驿站", "广州市从化区城郊街", 23.55, 113.58, 720.0, "https://example.com/images/hotel20.jpg", "13800138020", true));
            hotelHomestayRepository.saveAll(items);
        }
//        productRepository.deleteAll();
//        List<Product> products = new ArrayList<>();
//        products.add(makeProduct("新会陈皮干", 68.0, 1, "江门", "https://example.com/images/agri1.jpg", 120));
//        products.add(makeProduct("从化荔枝干", 45.0, 1, "广州", "https://example.com/images/agri2.jpg", 200));
//        products.add(makeProduct("增城丝苗米", 32.0, 2, "广州", "https://example.com/images/agri3.jpg", 150));
//        products.add(makeProduct("怀集砂糖橘", 26.0, 2, "肇庆", "https://example.com/images/agri4.jpg", 180));
//        products.add(makeProduct("英德红茶", 88.0, 3, "清远", "https://example.com/images/agri5.jpg", 90));
//        products.add(makeProduct("湛江海鸭蛋", 58.0, 3, "湛江", "https://example.com/images/agri6.jpg", 140));
//        products.add(makeProduct("阳春豆豉", 22.0, 4, "阳江", "https://example.com/images/agri7.jpg", 160));
//        products.add(makeProduct("连州菜心", 18.0, 4, "清远", "https://example.com/images/agri8.jpg", 220));
//        productRepository.saveAll(products);
    }

    private HotelHomestay makeHotel(String name, String addr, double lat, double lon, double price, String img, String phone, boolean available) {
        HotelHomestay h = new HotelHomestay();
        h.setHotelName(name);
        h.setAddress(addr);
        h.setLatitude(lat);
        h.setLongitude(lon);
        h.setPrice(price);
        h.setImageUrl(img);
        h.setContactNumber(phone);
        h.setLastUpdated(LocalDateTime.now());
        h.setIsAvailable(available);
        return h;
    }

    private Product makeProduct(String name, double price, int category, String origin, String imageUrl, int stock) {
        Product p = new Product();
        p.setProductName(name);
        p.setPrice(price);
        p.setCategory(category);
        p.setOrigin(origin);
        p.setTbImageUrl(imageUrl);
        p.setApproved(true);
        p.setStock(stock);
        p.setPublishedAt(LocalDateTime.now());
        return p;
    }
}
