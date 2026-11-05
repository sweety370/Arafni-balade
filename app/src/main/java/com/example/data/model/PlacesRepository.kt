package com.example.data.model

import com.example.ui.map.EgyptMapData
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object PlacesRepository {

    val EGYPT_BOUNDS_MIN_LAT = 21.8
    val EGYPT_BOUNDS_MAX_LAT = 31.9
    val EGYPT_BOUNDS_MIN_LNG = 24.5
    val EGYPT_BOUNDS_MAX_LNG = 37.0

    val CAIRO_LOCATION = GeoPoint(30.0444, 31.2357)

    // Master list of service POIs across Egypt
    val allServices: List<ServicePOI> = listOf(
        // Gas Stations
        ServicePOI("gas_cairo_1", "محطة وقود تشيل أوت - التحرير", ServiceType.GAS_STATION, GeoPoint(30.0440, 31.2380), "ميدان التحرير، وسط البلد", "مفتوح 24 ساعة"),
        ServicePOI("gas_cairo_2", "محطة وقود وطنية - المحور", ServiceType.GAS_STATION, GeoPoint(30.0250, 31.1400), "محور 26 يوليو، الجيزة", "مفتوح 24 ساعة"),
        ServicePOI("gas_cairo_3", "محطة وقود مصر للبترول - العباسية", ServiceType.GAS_STATION, GeoPoint(30.0650, 31.2800), "ميدان العباسية، القاهرة", "مفتوح 24 ساعة"),
        ServicePOI("gas_alex_1", "محطة وقود موبيل - الكورنيش", ServiceType.GAS_STATION, GeoPoint(31.2150, 29.9100), "طريق الجيش، الإسكندرية", "مفتوح 24 ساعة"),
        ServicePOI("gas_alex_2", "محطة وقود طاقة - سموحة", ServiceType.GAS_STATION, GeoPoint(31.1900, 29.9400), "ميدان فيكتور عمانويل، سموحة", "مفتوح 24 ساعة"),
        ServicePOI("gas_luxor_1", "محطة وقود التعاون - الأقصر", ServiceType.GAS_STATION, GeoPoint(25.6950, 32.6450), "شارع التليفزيون، الأقصر", "مفتوح 24 ساعة"),
        ServicePOI("gas_aswan_1", "محطة وقود وطنية - طريق السد", ServiceType.GAS_STATION, GeoPoint(24.0500, 32.8900), "طريق الخزان والسد العالي", "مفتوح 24 ساعة"),
        ServicePOI("gas_sharm_1", "محطة وقود شل - خليج نعمة", ServiceType.GAS_STATION, GeoPoint(27.9180, 34.3320), "طريق السلام، شرم الشيخ", "مفتوح 24 ساعة"),
        ServicePOI("gas_hurghada_1", "محطة وقود توتال - الممشى السياحي", ServiceType.GAS_STATION, GeoPoint(27.2200, 33.8300), "طريق القرى، الغردقة", "مفتوح 24 ساعة"),

        // Parking / Garages
        ServicePOI("park_cairo_1", "جراج التحرير الذكي متعدد الطوابق", ServiceType.PARKING, GeoPoint(30.0450, 31.2370), "ميدان التحرير، وسط القاهرة (سعة 1700 سيارة)", "متاح 24 ساعة"),
        ServicePOI("park_cairo_2", "جراج روكسي الإلكتروني", ServiceType.PARKING, GeoPoint(30.0920, 31.3200), "ميدان روكسي، مصر الجديدة (أوتوماتيكي)", "متاح 24 ساعة"),
        ServicePOI("park_cairo_3", "موقف محطة مصر ورمسيس", ServiceType.PARKING, GeoPoint(30.0630, 31.2480), "ميدان رمسيس، القاهرة", "متاح 24 ساعة"),
        ServicePOI("park_cairo_4", "جراج الأوبرا متعدد الطوابق", ServiceType.PARKING, GeoPoint(30.0425, 31.2260), "ساحة دار الأوبرا، الجزيرة", "متاح 24 ساعة"),
        ServicePOI("park_alex_1", "جراج محطة مصر بالإسكندرية", ServiceType.PARKING, GeoPoint(31.1920, 29.9050), "ميدان الشهداء، الإسكندرية", "متاح 24 ساعة"),
        ServicePOI("park_alex_2", "موقف المنشية والقلعة", ServiceType.PARKING, GeoPoint(31.2050, 29.8900), "ميدان المنشية وكورنيش الأنفوشي", "متاح 24 ساعة"),

        // Restaurants
        ServicePOI("rest_cairo_1", "مطعم كشري أبو طارق التراثي", ServiceType.RESTAURANT, GeoPoint(30.0520, 31.2390), "شارع معروف، وسط البلد، القاهرة", "من 9 ص حتى 1 ص"),
        ServicePOI("rest_cairo_2", "مطاعم المشويات والقرية النوبية", ServiceType.RESTAURANT, GeoPoint(29.9800, 31.1450), "طريق سقارة والمنصورية، الهرم", "من 11 ص حتى منتصف الليل"),
        ServicePOI("rest_alex_1", "مطعم قدورة للأسماك الطازجة", ServiceType.RESTAURANT, GeoPoint(31.2110, 29.8850), "شارع 26 يوليو، بحري، الإسكندرية", "من 10 ص حتى 2 ص"),
        ServicePOI("rest_alex_2", "مطعم بلبع للمشويات والبحريات", ServiceType.RESTAURANT, GeoPoint(31.2400, 29.9700), "طريق الجيش، سيدي جابر وميامي", "من 11 ص حتى 3 ص"),
        ServicePOI("rest_luxor_1", "مطعم المرزبان للمأكولات الصعيدية", ServiceType.RESTAURANT, GeoPoint(25.6980, 32.6390), "شارع خالد بن الوليد، كورنيش النيل", "من 12 ظ حتى منتصف الليل"),
        ServicePOI("rest_aswan_1", "مطعم الدكة النوبي بجزيرة إيزيس", ServiceType.RESTAURANT, GeoPoint(24.0850, 32.8850), "جزيرة وسط النيل، أسوان", "من 1 ظ حتى 11 م"),

        // Cafes
        ServicePOI("cafe_cairo_1", "مقهى الفيشاوي التاريخي", ServiceType.CAFE, GeoPoint(30.0478, 31.2620), "خان الخليلي، القاهرة الفاطمية (تأسس 1797)", "مفتوح 24 ساعة"),
        ServicePOI("cafe_alex_1", "مقهى ديليس العريق (Délices)", ServiceType.CAFE, GeoPoint(31.2010, 29.9000), "محطة الرمل، الإسكندرية (تأسس 1922)", "من 8 ص حتى 1 ص"),
        ServicePOI("cafe_aswan_1", "كافيه تراس أولد كتاراكت", ServiceType.CAFE, GeoPoint(24.0750, 32.8870), "فندق كتاراكت التاريخي، أسوان", "من 9 ص حتى 11 م"),

        // Hospitals
        ServicePOI("hosp_cairo_1", "مستشفى القصر العيني التعليمي", ServiceType.HOSPITAL, GeoPoint(30.0310, 31.2290), "شارع القصر العيني، القاهرة", "طوارئ 24 ساعة"),
        ServicePOI("hosp_cairo_2", "مستشفى معهد ناصر للبحوث والعلاج", ServiceType.HOSPITAL, GeoPoint(30.0800, 31.2400), "كورنيش النيل، شبرا، القاهرة", "طوارئ 24 ساعة"),
        ServicePOI("hosp_alex_1", "المستشفى الرئيسي الجامعي بالإسكندرية", ServiceType.HOSPITAL, GeoPoint(31.2050, 29.9150), "الشاطبي، الإسكندرية", "طوارئ 24 ساعة"),
        ServicePOI("hosp_luxor_1", "مستشفى الأقصر الدولي", ServiceType.HOSPITAL, GeoPoint(25.6850, 32.6350), "شارع التليفزيون، الأقصر", "طوارئ 24 ساعة"),
        ServicePOI("hosp_aswan_1", "مستشفى أسوان الجامعي الجديد", ServiceType.HOSPITAL, GeoPoint(24.0950, 32.9050), "طريق السادات، أسوان", "طوارئ 24 ساعة")
    )

    fun districtToPlace(dist: EgyptDistrict): Place {
        return Place(
            id = dist.id,
            nameAr = dist.nameAr,
            nameEn = dist.centerTypeAr,
            governorateAr = dist.governorateAr,
            category = PlaceCategory.HISTORIC_CITIES,
            location = dist.location,
            shortDescriptionAr = "${dist.nameAr} - ${dist.centerTypeAr} بمحافظة ${dist.governorateAr}.",
            namingOriginAr = dist.namingOriginAr,
            establishedDateAr = dist.establishedDateAr,
            whyEstablishedAr = dist.whyEstablishedAr,
            egyptAffiliationAr = dist.egyptAffiliationAr,
            historicEventsAr = dist.historicEventsAr,
            rulersAndKingsAr = dist.rulersAndKingsAr,
            famousBornAr = dist.famousBornAr,
            visitingInfoAr = "مركز ومدينة حيوية مأهولة، تضم مقرات إدارية وشوارع تجارية ومحطات نقل ومستشفيات ومدارس ومعالم تاريخية.",
            ticketPriceAr = "التجول والزيارة مجاناً",
            openingHoursAr = "مفتوحة على مدار الساعة",
            interestingFactsAr = listOf(
                "تعد من أهم المراكز والمواقع الحيوية بمحافظة ${dist.governorateAr}.",
                "تتميز بطابع تراثي وعمراني فريد وشبكة مواصلات تربطها بقرى ومدن المحافظة."
            ),
            bestTimeToVisitAr = "على مدار العام",
            defaultRouteFromCairo = RouteInfo(
                distanceKm = 40,
                durationCarMinutes = 45,
                durationBusMinutes = 70,
                durationTrainMinutes = null,
                mainHighwaysAr = listOf("طرق ومحاور محافظة ${dist.governorateAr}"),
                waypoints = listOf(CAIRO_LOCATION, dist.location),
                routeStops = listOf(
                    RouteStop("نقطة الانطلاق (القاهرة)", 0, "البداية"),
                    RouteStop(dist.nameAr, 40, "الوصول للمركز")
                ),
                travelTipsAr = listOf("تتوفر سيارات الأجرة والميكروباص والقطارات التي تصل مباشرة إلى المركز.")
            )
        )
    }

    fun governorateToPlace(gov: CityLabel): Place {
        return Place(
            id = "gov_${gov.nameAr}",
            nameAr = "محافظة ${gov.nameAr}",
            nameEn = gov.regionAr,
            governorateAr = gov.nameAr,
            category = PlaceCategory.HISTORIC_CITIES,
            location = gov.location,
            shortDescriptionAr = "محافظة ${gov.nameAr} - ${gov.regionAr} بجمهورية مصر العربية.",
            namingOriginAr = "سُميت محافظة ${gov.nameAr} بهذا الاسم التاريخي الراسخ تيمناً بمدينتها الرئيسية وعاصمتها التاريخية، ولكل اسم قصة فرعونية أو عربية أصيلة تعبر عن موقعها وطبيعتها الجغرافية.",
            establishedDateAr = "إقليم مصري تاريخي قديم ومأهول منذ فجر التاريخ الفرعوني ومقاطعات مصر القديمة الـ42.",
            whyEstablishedAr = "أُنشئت كوحدة حكم محلية وإدارية وتنظيمية لتنمية وتأمين مجتمعات الإقليم وتوفير الخدمات الزراعية والصناعية والتجارية للمواطنين.",
            egyptAffiliationAr = "جزء لا يتجزأ من تراب الوطن المصري منذ توحيد القطرين على يد الملك مينا نارمر عام 3100 قبل الميلاد.",
            historicEventsAr = "شهدت أرض المحافظة عبر القرون معارك وأحداثاً وطنية ساهمت في صون وحدة واستقلال مصر وبناء نهضتها الحديثة.",
            rulersAndKingsAr = "تعاقب على إدارتها وحمايتها ملوك الفراعنة وسلاطين مصر وقادتها الوطنيون.",
            famousBornAr = "خرج من أرضها نخبة من كبار العلماء والفقهاء والأدباء والقادة العسكريين والمفكرين في تاريخ مصر.",
            visitingInfoAr = "تضم مدناً ومراكز إدارية متطورة وشبكة طرق قومية حديثة تربطها بجميع أنحاء الجمهورية.",
            ticketPriceAr = "الزيارة والتنقل متاح للجميع",
            openingHoursAr = "على مدار الساعة",
            interestingFactsAr = listOf(
                "تعد من الركائز التنموية الكبرى في ${gov.regionAr}.",
                "تتميز بتنوع ثري يجمع بين التراث والأصالة والمشروعات الحديثة."
            ),
            bestTimeToVisitAr = "على مدار العام",
            defaultRouteFromCairo = RouteInfo(
                distanceKm = 60,
                durationCarMinutes = 60,
                durationBusMinutes = 90,
                durationTrainMinutes = null,
                mainHighwaysAr = listOf("شبكة الطرق والمحاور القومية السريعة"),
                waypoints = listOf(CAIRO_LOCATION, gov.location),
                routeStops = listOf(
                    RouteStop("نقطة الانطلاق (القاهرة)", 0, "البداية"),
                    RouteStop(gov.nameAr, 60, "الوصول للمحافظة")
                ),
                travelTipsAr = listOf("استكشف المراكز والقرى التابعة للمحافظة للتعرف على تراثها الأصيل.")
            )
        )
    }

    val places: List<Place> = listOf(
        Place(
            id = "giza_pyramids",
            nameAr = "أهرامات الجيزة وأبو الهول",
            nameEn = "Giza Pyramids & Sphinx",
            governorateAr = "الجيزة",
            category = PlaceCategory.PHARAONIC,
            location = GeoPoint(29.9792, 31.1342),
            shortDescriptionAr = "إحدى عجائب الدنيا السبع القديمة الباقية، مقابر ملوك الفراعنة خوفو وخفرع ومنكاورع.",
            namingOriginAr = "سُميت بأهرامات الجيزة نسبة إلى مدينة الجيزة، وكلمة 'الجيزة' في لسان العرب تعني 'المكان المجتاز أو سفح الجبل'، وسماها قدماء المصريين 'آخت خوفو' أي 'أفق خوفو النوراني'. أما اسم 'أبو الهول' فأصله الفرعوني 'بو حول' أي مكان الإله حول، وحُرّف بالقبطية إلى 'بلهيب' ثم عربياً إلى 'أبو الهول' تعبيراً عن هيبته وسطوته ورهبته.",
            establishedDateAr = "شُيدت خلال عصر الأسرة الفرعونية الرابعة حوالي عام 2560 قبل الميلاد (قبل أكثر من 4580 عاماً)، واستغرق بناء هرم خوفو الأكبر قرابة 20 عاماً بجهد 100 ألف مهندس وبناء مصري قديم.",
            whyEstablishedAr = "أُنشئت لتكون مقابر ملكية مقدسة تضمن خلود ملوك الفراعنة وصعود أرواحهم إلى السماء وعرش رع، ولتكون أعظم مجمع جنائزي ديني ومعماري يخلد عظمة الدولة المصرية القديمة.",
            egyptAffiliationAr = "مهد الحضارة المصرية القديمة وأعظم رمز للهوية والسيادة الوطنية لمصر عبر آلاف السنين.",
            historicEventsAr = "شهدت هضبة الأهرام محطات تاريخية عالمية: زيارة الإسكندر الأكبر عام 331 ق.م، معركة الأهرام الشهيرة عام 1798 بين قوات نابليون بونابرت والمماليك، ودراسة علماء الحملة الفرنسية ووضع أول خريطة تفصيلية لوصف مصر.",
            rulersAndKingsAr = "مات ودُفن هنا أعظم ملوك الدولة القديمة: الملك خوفو (صاحب الهرم الأكبر)، والملك خفرع (ابنه وباني الهرم الثاني)، والملك منكاورع (باني الهرم الثالث)، بالإضافة إلى مقابر العمال بناة الأهرام والوزير حم إيونو مهندس الهرم الأكبر.",
            famousBornAr = "الأمير والمهندس العبقري 'حم إيونو' (باني الهرم الأكبر والمشرف على أشغال الملك)، والأميرة ميريت إيتس، والملك منقرع، ونخبة كهنة رع في عين شمس القديمة والجيزة.",
            visitingInfoAr = "مفتوح يومياً للمصريين والأجانب. تتوفر حافلات كهربائية صديقة للبيئة داخل الهضبة ومسارات سير ممهدة وكافتيريات سياحية.",
            ticketPriceAr = "للمصريين 60 جنيهاً (الطلاب 30) - للأجانب 540 جنيهاً",
            openingHoursAr = "من 8:00 صباحاً حتى 5:00 مساءً",
            interestingFactsAr = listOf(
                "الهرم الأكبر يحوي 2.3 مليون حجر جيري وجرانيتي بوزن إجمالي يناهز 6 ملايين طن.",
                "قاعدة الهرم الأكبر متساوية الأضلاع بدقة متناهية تواجه الجهات الأصلية الأربع بدقة زاوية تقل عن جزء من الدرجة.",
                "أبو الهول منحوت من صخرة جيرية طبيعية واحدة يبلغ طولها 73 متراً وارتفاعها 20 متراً."
            ),
            bestTimeToVisitAr = "من أكتوبر حتى أبريل (الصباح الباكر لتجنب حرارة الظهيرة)",
            defaultRouteFromCairo = RouteInfo(
                distanceKm = 18,
                durationCarMinutes = 35,
                durationBusMinutes = 55,
                durationTrainMinutes = null,
                mainHighwaysAr = listOf("الطريق الدائري", "طريق المنصورية السياحي", "شارع الهرم"),
                waypoints = listOf(
                    GeoPoint(30.0444, 31.2357),
                    GeoPoint(30.0131, 31.2089),
                    GeoPoint(29.9950, 31.1500),
                    GeoPoint(29.9792, 31.1342)
                ),
                routeStops = listOf(
                    RouteStop("ميدان التحرير / وسط القاهرة", 0, "نقطة الانطلاق"),
                    RouteStop("محور صفط اللبن ومطلع الدائري", 7, "شريان رئيسي"),
                    RouteStop("طريق المنصورية وميدان الرماية", 14, "المدخل السياحي"),
                    RouteStop("بوابة هضبة الأهرام الرسمية", 18, "الوصول للموقع")
                ),
                travelTipsAr = listOf("احجز التذاكر عبر موقع وزارة السياحة والآثار الرسمي لتفادي طوابير الانتظار.")
            )
        ),
        Place(
            id = "cairo_citadel",
            nameAr = "قلعة صلاح الدين الأيوبي وجامع محمد علي",
            nameEn = "Citadel of Saladin & Mosque of Muhammad Ali",
            governorateAr = "القاهرة",
            category = PlaceCategory.ISLAMIC_COPTIC,
            location = GeoPoint(30.0299, 31.2611),
            shortDescriptionAr = "قلعة حربية منيعة شيدت على جبل المقطم لتكون مقراً لحكم مصر وحصناً ضد الصليبيين.",
            namingOriginAr = "سُميت باسم مؤسسها السلطان الناصر صلاح الدين الأيوبي قاهر الصليبيين ومحرر القدس عام 1176م. كما تُعرف بـ'قلعة الجبل' لأنها شُيدت فوق هضبة جبل المقطم الشاهقة لتشرف على كافة جهات القاهرة.",
            establishedDateAr = "بدأ تشييدها القائد صلاح الدين الأيوبي وأشرف عليها وزيره بهاء الدين قراقوش عام 1176م (572هـ)، واكتملت مبانيها وأسوارها في عهد السلطان الكامل بن العادل عام 1207م وظلت مقراً رسمياً لحكم مصر قرابة 700 عام.",
            whyEstablishedAr = "أنشئت لتكون حصناً عسكرياً منيعاً ومقراً لإدارة وحكم مصر وحماية العاصمة من أطماع الغزوات الصليبية والمغولية وتوفير قاعدة آمنة للجيوش المصرية.",
            egyptAffiliationAr = "مقر القيادة السياسية والعسكرية للدولة المصرية طوال 7 قرون متواصلة.",
            historicEventsAr = "صد حملات الفرنجة، إدارة معارك عين جالوت ضد المغول عام 1260م من قِبل السلطان قطز، مذبحة القلعة الشهيرة عام 1811م، وتشييد جامع المرمر العثماني الفخم عام 1848م.",
            rulersAndKingsAr = "مات وحكم بها سلاطين وملوك عظام: السلطان العادل الأيوبي، والملك الصالح نجم الدين أيوب، وشجر الدر (أول حاكمة في الإسلام)، والظاهر بيبرس، ودُفن بداخلها والي مصر محمد علي باشا.",
            famousBornAr = "أمراء وسلاطين مصر وقادة الجيوش المصرية وكبار المؤرخين كالمقريزي وابن إياس.",
            visitingInfoAr = "تضم جامع محمد علي، ومتحف الشرطة، والمتحف الحربي القومي، وبئر يوسف المحفورة بعمق 90 متراً في قلب الصخر.",
            ticketPriceAr = "للمصريين 60 جنيهاً (الطلاب 30) - للأجانب 450 جنيهاً",
            openingHoursAr = "من 9:00 صباحاً حتى 5:00 مساءً",
            interestingFactsAr = listOf(
                "جامع محمد علي مكسو برخام الألبستر المستخرج من محاجر بني سويف ومآذنه ترتفع 84 متراً.",
                "بئر يوسف حفرت داخل الصخر بعمق 90 متراً بواسطة أسرى الحروب لتأمين الماء وقت الحصار.",
                "يمكن من شرفة القلعة البانورامية مشاهدة القاهرة بأسرها وأهرامات الجيزة في الأيام الصافية."
            ),
            bestTimeToVisitAr = "فترة بعد العصر لمشاهدة غروب الشمس فوق قاهرة المعز",
            defaultRouteFromCairo = RouteInfo(
                distanceKm = 7,
                durationCarMinutes = 20,
                durationBusMinutes = 35,
                durationTrainMinutes = null,
                mainHighwaysAr = listOf("طريق صلاح سالم", "محور الأوتوستراد"),
                waypoints = listOf(
                    GeoPoint(30.0444, 31.2357),
                    GeoPoint(30.0380, 31.2450),
                    GeoPoint(30.0299, 31.2611)
                ),
                routeStops = listOf(
                    RouteStop("ميدان التحرير", 0, "البداية"),
                    RouteStop("كوبري الأزهر وميدان الأوبرا", 3, "وسط المدينة"),
                    RouteStop("بوابة قلعة الجبل - طريق صلاح سالم", 7, "الوصول")
                ),
                travelTipsAr = listOf("التقاط صور بانورامية من ساحة جامع محمد علي المطلة على مآذن القاهرة.")
            )
        ),
        Place(
            id = "cairo_tower",
            nameAr = "برج القاهرة (جزيرة الزمالك)",
            nameEn = "Cairo Tower",
            governorateAr = "القاهرة",
            category = PlaceCategory.MODERN_CULTURE,
            location = GeoPoint(30.0459, 31.2243),
            shortDescriptionAr = "صرح وطني شيد بهيئة زهرة اللوتس المصرية على ضفاف النيل بالزمالك وبارتفاع 187 متراً.",
            namingOriginAr = "سُمي 'برج القاهرة' لأنه بُني ليكون الأيقونة المعمارية الأبرز لعاصمة جمهورية مصر العربية على جزيرة الزمالك النيلية. وكان الرئيس جمال عبد الناصر يطلق عليه رمزياً تعبيراً عن كرامة مصر واستقلال قرارها الوطني.",
            establishedDateAr = "شُيد بين عامي 1956 و1961 وافتتح رسمياً في 11 أبريل 1961 في عهد الرئيس الراحل جمال عبد الناصر، وصممه المهندس المعماري المصري العبقري نعوم شبيب من الخرسانة المسلحة المفرغة بشكل شبكة مستوحاة من نبات اللوتس الفرعوني.",
            whyEstablishedAr = "أنشئ كصرح وطني للبث الإذاعي والاتصالات ومقصد سياحي مدني يؤكد استقلال الإرادة المصرية وبأموال الدعم التي حاول الجانب الأمريكي استغلالها سياسياً فحولتها مصر إلى برج سياحي تاريخي.",
            egyptAffiliationAr = "رمز السيادة والشموخ الوطني المصري في العصر الجمهوري الحديث.",
            historicEventsAr = "ارتبط بناؤه بمرحلة الكفاح الوطني ضد الضغوط الأجنبية في الخمسينيات، وأصبح موقعاً لافتتاح المهرجانات الدولية والمناسبات الوطنية ومقصداً للملوك ورؤساء الدول الزائرين لمصر.",
            rulersAndKingsAr = "افتتحه الرئيس جمال عبد الناصر وزارته شخصيات عالمية تاريخية مثل يوري جاجارين (أول رائد فضاء في التاريخ) وجواهر لال نهرو والمارشال تيتو.",
            famousBornAr = "صممه المهندس المصري العالمي نعوم شبيب (أحد رواد العمارة الحديثة في مصر).",
            visitingInfoAr = "يحتوي على مطعم دوار يدور دورة كاملة كل ساعة، وكافيتريا بانورامية ومنصة تلسكوبات لمشاهدة القاهرة وضواحيها بالكامل.",
            ticketPriceAr = "للمصريين 70 جنيهاً - للأجانب 250 جنيهاً",
            openingHoursAr = "من 9:00 صباحاً حتى 1:00 بعد منتصف الليل",
            interestingFactsAr = listOf(
                "يزيد ارتفاعه عن الهرم الأكبر بنحو 48 متراً ويقف على قاعدة من حجر الجرانيت الأسواني الأصيل.",
                "يتكون البرج من 16 طابقاً ويستند على قاعدة خرسانية ضخمة تمتد لأعماق الصخر تحت مجرى نهر النيل.",
                "يضم 8 ملايين حجر فسيفساء صغيرة تغطي واجهته الخارجية المفرغة بهيئة زهرة اللوتس."
            ),
            bestTimeToVisitAr = "ساعة الغروب للاستمتاع بانعكاس أشعة الشمس الذهبية على صفحة النيل وأضواء القاهرة الليلية",
            defaultRouteFromCairo = RouteInfo(
                distanceKm = 3,
                durationCarMinutes = 10,
                durationBusMinutes = 20,
                durationTrainMinutes = null,
                mainHighwaysAr = listOf("كوبري قصر النيل", "شارع الجزيرة بالزمالك"),
                waypoints = listOf(
                    GeoPoint(30.0444, 31.2357),
                    GeoPoint(30.0435, 31.2285),
                    GeoPoint(30.0459, 31.2243)
                ),
                routeStops = listOf(
                    RouteStop("ميدان التحرير", 0, "البداية"),
                    RouteStop("كوبري قصر النيل (أسود قصر النيل)", 1, "عبور النيل"),
                    RouteStop("بوابة برج القاهرة بحديقة الأندلس", 3, "الوصول")
                ),
                travelTipsAr = listOf("يفضل حجز تذكرة الصعود السريع لتفادي الانتظار في العطلات الأسبوعية.")
            )
        ),
        Place(
            id = "qaitbay_citadel",
            nameAr = "قلعة قايتباي بالإسكندرية",
            nameEn = "Citadel of Qaitbay",
            governorateAr = "الإسكندرية",
            category = PlaceCategory.ISLAMIC_COPTIC,
            location = GeoPoint(31.2140, 29.8856),
            shortDescriptionAr = "قلعة بحرية شامخة شيدت بحجارة فنار الإسكندرية القديم لتحصين الساحل الشمالي.",
            namingOriginAr = "سُميت نسبة إلى مؤسسها السلطان المملوكي الأشرف أبو النصر قايتباي المحمودي، والذي حكم مصر والشام وأنشأ القلعة عام 1477م على أطلال فنار الإسكندرية المنهار لحماية الثغور المصرية من الغزو البحري العثماني.",
            establishedDateAr = "شُرع في بنائها عام 1477م (882هـ) وانتهى بناؤها عام 1479م. واستُخدمت في تشييدها أحجار الجرانيت الضخمة المتبقية من فنار الإسكندرية القديم إحدى عجائب الدنيا السبع القديمة.",
            whyEstablishedAr = "أنشئت كحصن دفاعي بحري استراتيجي على لسان الميناء الشرقي لحماية الإسكندرية والسواحل المصرية من غارات القراصنة وأساطيل البندقية وجنوة والعثمانيين.",
            egyptAffiliationAr = "الثغر الشمالي الأعظم لحماية حدود الدولة المصرية البحرية.",
            historicEventsAr = "شهدت القلعة معارك بحرية ضارية، كما كانت مسرحاً لقصف الأسطول البريطاني لمدينة الإسكندرية في 11 يوليو 1882 أثناء الثورة العرابية ووقوف أبطال المدفعية المصرية دفاعاً عن الوطن.",
            rulersAndKingsAr = "السلطان قايتباي، والسلطان قانصوه الغوري الذي جدد حاميتها العسكرية وزودها بالمدافع والبارود عام 1501م، والخديوي إسماعيل، والملك فؤاد الأول الذي أشرف على ترميمها الشامل.",
            famousBornAr = "ارتبطت بأعلام الإسكندرية وقادة البحرية المصرية والمؤرخين.",
            visitingInfoAr = "تقع في لسان بحري طبيعي بالأنفوشي تحيط به مياه البحر المتوسط الصافية وممشى الكورنيش ومطاعم الأسماك الشهيرة.",
            ticketPriceAr = "للمصريين 60 جنيهاً (الطلاب 30) - للأجانب 150 جنيهاً",
            openingHoursAr = "من 9:00 صباحاً حتى 5:00 مساءً",
            interestingFactsAr = listOf(
                "أقيمت القلعة في نفس الموقع الدقيق لفنار الإسكندرية الأسطوري.",
                "تتكون من ثلاثة طوابق حجرية مربعة وبرج رئيسي ومسجد أثري وخنادق دفاعية.",
                "تضم أول وأقدم مسجد شُيد داخل قلعة عسكرية ساحلية على البحر الأبيض المتوسط."
            ),
            bestTimeToVisitAr = "ساعات الصباح والغروب للاستمتاع بنسيم البحر الأبيض المتوسط",
            defaultRouteFromCairo = RouteInfo(
                distanceKm = 225,
                durationCarMinutes = 140,
                durationBusMinutes = 180,
                durationTrainMinutes = 130,
                mainHighwaysAr = listOf("طريق القاهرة - الإسكندرية الصحراوي", "محور التعمير", "طريق الجيش (الكورنيش)"),
                waypoints = listOf(
                    GeoPoint(30.0444, 31.2357),
                    GeoPoint(30.2500, 30.7000),
                    GeoPoint(30.6000, 30.3000),
                    GeoPoint(31.0000, 29.9500),
                    GeoPoint(31.2140, 29.8856)
                ),
                routeStops = listOf(
                    RouteStop("بوابات القاهرة الصحراوي", 0, "البداية"),
                    RouteStop("استراحة وادي النطرون", 105, "استراحة وتزود بالوقود"),
                    RouteStop("بوابات رسوم العامرية", 195, "مدخل الإسكندرية"),
                    RouteStop("محور التعمير والأنفوشي", 218, "قلب المدينة"),
                    RouteStop("بوابة قلعة قايتباي البحرية", 225, "الوصول")
                ),
                travelTipsAr = listOf("يتوفر قطار تالجو السريع من محطة مصر برمسيس لمحطة سيدي جابر ومحطة مصر بالإسكندرية في ساعتين فقط.")
            )
        ),
        Place(
            id = "karnak_temple",
            nameAr = "معبد الكرنك العظيم بالأقصر",
            nameEn = "Karnak Temple Complex",
            governorateAr = "الأقصر",
            category = PlaceCategory.PHARAONIC,
            location = GeoPoint(25.7188, 32.6573),
            shortDescriptionAr = "أكبر دار عبادة شُيدت في العالم القديم، استمر بناؤها وتطويرها لأكثر من ألفي عام.",
            namingOriginAr = "سُمي بالكرنك نسبة إلى قرية الكرنك المجاورة له والمشتق اسمها من الكلمة العربية 'الخورنق' أو 'الكرنك' وتعني القرية الحصينة ذات الأسوار المنيعة. وسماه الفراعنة القدماء 'إبت سوت' أي 'المكان الأكثر قدسية واختياراً'.",
            establishedDateAr = "بدأ بناؤه في عهد الدولة الوسطى (الأسرة الحادية عشرة حوالي عام 2000 قبل الميلاد) وتوالى على توسيعه وتشييد صروحه ملوك مصر لأكثر من 2000 عام حتى العصر البطلمي والروماني.",
            whyEstablishedAr = "أنشئ ليكون المجمع الديني والكهنوتي الإمبراطوري الأعظم للإله آمون رع وعاصمة الطقوس الملكية وتخليد انتصارات فراعنة مصر وتتويج ملوكها.",
            egyptAffiliationAr = "أعظم صرح ديني ومعماري يوثق تاريخ الدولة المصرية القديمة وعاصمتها الإمبراطورية طيبة.",
            historicEventsAr = "شهد أهم الاحتفالات الدينية الملكية في مصر القديمة كعيد 'الأوبت' عبر طريق الكباش، وتسجيل انتصارات تحتمس الثالث في معركة مجدو، وتتويج رمسيس الثاني وحروب سيتي الأول.",
            rulersAndKingsAr = "الملكة حتشبسوت، الملك تحتمس الثالث، الملك سيتي الأول، والملك رمسيس الثاني.",
            famousBornAr = "فراعنة الأسرة الثامنة عشرة والتاسعة عشرة الذين بنوا الإمبراطورية المصرية العظمى.",
            visitingInfoAr = "يقع على الضفة الشرقية للنيل، يضم صالة الأعمدة الكبرى (134 عموداً) والبحيرة المقدسة وتمثال الجعران المقدس.",
            ticketPriceAr = "للمصريين 40 جنيهاً (الطلاب 20) - للأجانب 450 جنيهاً",
            openingHoursAr = "من 6:00 صباحاً حتى 5:30 مساءً",
            interestingFactsAr = listOf(
                "صالة الأعمدة الكبرى تضم 134 عموداً حجرياً ضخماً ترتفع 21 متراً وتشبه غابة من نبات البردي.",
                "مساحة مجمع الكرنك تزيد عن 200 فدان وهو أضخم مجمع ديني شيده الإنسان في التاريخ القديم.",
                "يرتبط معبد الكرنك بمعبد الأقصر عبر 'طريق الكباش' الأثري البالغ طوله 2.7 كيلومتر."
            ),
            bestTimeToVisitAr = "من أكتوبر حتى مارس (الطقس الشتوي المشمس الدافئ)",
            defaultRouteFromCairo = RouteInfo(
                distanceKm = 660,
                durationCarMinutes = 450,
                durationBusMinutes = 540,
                durationTrainMinutes = 570,
                mainHighwaysAr = listOf("طريق الصعيد الصحراوي الغربي السريع", "طريق قنا - الأقصر الصحراوي"),
                waypoints = listOf(
                    GeoPoint(30.0444, 31.2357),
                    GeoPoint(29.0000, 31.0000),
                    GeoPoint(27.5000, 31.1000),
                    GeoPoint(26.1500, 32.7000),
                    GeoPoint(25.7188, 32.6573)
                ),
                routeStops = listOf(
                    RouteStop("بوابة القاهرة الصحراوي الغربي", 0, "البداية"),
                    RouteStop("استراحة بني سويف والمنيا", 190, "محطة وسيطة"),
                    RouteStop("محافظة أسيوط", 350, "منتصف الطريق"),
                    RouteStop("محافظة قنا", 580, "شمال الأقصر"),
                    RouteStop("معبد الكرنك بالأقصر", 660, "الوصول")
                ),
                travelTipsAr = listOf("حضور عرض الصوت والضوء الأسطوري بالكرنك ليلاً باللغة العربية.")
            )
        ),
        Place(
            id = "abu_simbel_temples",
            nameAr = "معبدا أبو سمبل جنوب أسوان",
            nameEn = "Abu Simbel Temples",
            governorateAr = "أسوان",
            category = PlaceCategory.PHARAONIC,
            location = GeoPoint(22.3372, 31.6258),
            shortDescriptionAr = "أعظم إعجاز فلكي ومعماري للملك رمسيس الثاني وحبيبته نفرتاري على ضفاف بحيرة ناصر.",
            namingOriginAr = "سُمي أبو سمبل نسبة إلى طفل مصري نوبي محلي اسمه 'أبو سمبل' كان يرشد الرحالة والمستكشف يوهان لودفيج بوركهارت عام 1813م إلى موقع المعبد المدفون تحت الرمال، فخلد المستكشفون اسمه على المعبد تكريماً له.",
            establishedDateAr = "نُحت في قلب الجبل الصخري حوالي عام 1264 قبل الميلاد في عهد الملك رمسيس الثاني، واستغرق نحته بالكامل نحو 20 عاماً لتخليد انتصار مصر في معركة قادش وسلامها مع الحيثيين.",
            whyEstablishedAr = "أنشئ لتأكيد السيادة المصرية على حدودها الجنوبية في النوبة، وتخليد عبقرية رمسيس الثاني العسكرية وظاهرة تعامد الشمس على وجهه داخل قدس الأقداس.",
            egyptAffiliationAr = "الدرع الجنوبي لمصر وحامية وادي النيل الفرعوني.",
            historicEventsAr = "شهد توقيع أول معاهدة سلام موثقة في التاريخ البشري، وظاهرة تعامد الشمس مرتين سنوياً (22 أكتوبر و22 فبراير)، وملحمة إنقاذ ونقل المعبد بالكامل عام 1968 برعاية اليونسكو.",
            rulersAndKingsAr = "الملك رمسيس الثاني (فرعون المجد والانتصارات)، وزوجته الملكة المحبوبة نفرتاري.",
            famousBornAr = "بناة ونحاتو الجبال في الجيش المصري والنوبي القديم.",
            visitingInfoAr = "يقع على بعد 280 كم جنوب غرب أسوان بالقرب من حدود مصر الجنوبية مع السودان، ويطل مباشرة على بحيرة ناصر العذبة.",
            ticketPriceAr = "للمصريين 50 جنيهاً (الطلاب 25) - للأجانب 500 جنيهاً",
            openingHoursAr = "من 6:00 صباحاً حتى 5:00 مساءً",
            interestingFactsAr = listOf(
                "تتعامد الشمس مرتين سنوياً (22 أكتوبر و22 فبراير) على وجه تمثال رمسيس الثاني في قدس الأقداس.",
                "تم تقطيع المعبد الصخري بوزن 250 ألف طن ونقله لأعلى الجبل مسافة 65 متراً دون خطأ مليمتر واحد.",
                "واجهة المعبد تحوي 4 تماثيل جالسة للملك رمسيس بارتفاع 20 متراً لكل تمثال."
            ),
            bestTimeToVisitAr = "يومي تعامد الشمس (22 أكتوبر و22 فبراير) وأشهر الشتاء المعتدلة",
            defaultRouteFromCairo = RouteInfo(
                distanceKm = 1160,
                durationCarMinutes = 760,
                durationBusMinutes = 900,
                durationTrainMinutes = null,
                mainHighwaysAr = listOf("طريق الصعيد الصحراوي الغربي", "طريق أسوان - أبو سمبل الدولي المزدوج"),
                waypoints = listOf(
                    GeoPoint(30.0444, 31.2357),
                    GeoPoint(26.0000, 32.0000),
                    GeoPoint(24.0889, 32.8998),
                    GeoPoint(23.2000, 32.2000),
                    GeoPoint(22.3372, 31.6258)
                ),
                routeStops = listOf(
                    RouteStop("القاهرة", 0, "البداية"),
                    RouteStop("مدينة أسوان", 880, "عاصمة الجنوب"),
                    RouteStop("مطار أسوان الدولي ومفيض توشكى", 940, "الصحراء الجنوبية"),
                    RouteStop("استراحة بحيرة ناصر", 1040, "محطة وقود"),
                    RouteStop("معابد أبو سمبل على بحيرة ناصر", 1160, "الوصول")
                ),
                travelTipsAr = listOf("توجد رحلات طيران مباشرة من مطار القاهرة ومطار أسوان إلى مطار أبو سمبل.")
            )
        )
    )

    fun getPlaceById(id: String): Place? {
        val found = places.find { it.id == id }
        if (found != null) return found

        val dist = EgyptMapData.ALL_CENTERS.find { it.id == id }
        if (dist != null) return districtToPlace(dist)

        return null
    }

    fun getPlacesByCategory(category: PlaceCategory): List<Place> {
        val direct = places.filter { it.category == category }
        val districts = EgyptMapData.ALL_CENTERS.map { districtToPlace(it) }.filter { it.category == category }
        return direct + districts
    }

    fun getPlacesByGovernorate(governorate: String): List<Place> {
        val direct = places.filter { it.governorateAr.contains(governorate) }
        val districts = EgyptMapData.ALL_CENTERS.filter { it.governorateAr.contains(governorate) }.map { districtToPlace(it) }
        return direct + districts
    }

    fun searchPlaces(query: String): List<Place> {
        if (query.isBlank()) return places
        val clean = query.trim().lowercase()

        val matchedPlaces = places.filter {
            it.nameAr.contains(clean) ||
            it.nameEn.lowercase().contains(clean) ||
            it.governorateAr.contains(clean) ||
            it.shortDescriptionAr.contains(clean) ||
            it.namingOriginAr.contains(clean) ||
            it.historicEventsAr.contains(clean)
        }

        val matchedDistricts = EgyptMapData.ALL_CENTERS.filter {
            it.nameAr.contains(clean) ||
            it.governorateAr.contains(clean) ||
            it.centerTypeAr.contains(clean) ||
            it.namingOriginAr.contains(clean) ||
            it.historicEventsAr.contains(clean) ||
            it.famousBornAr.contains(clean)
        }.map { districtToPlace(it) }

        return matchedPlaces + matchedDistricts
    }

    // Find nearest center, place, or governorate when clicking ANY point on Egypt map
    fun getNearestPlace(point: GeoPoint): Place {
        var minDistance = Double.MAX_VALUE
        var bestPlace: Place? = null

        // 1. Check all administrative centers & districts first
        EgyptMapData.ALL_CENTERS.forEach { dist ->
            val distKm = calculateDistanceKm(point.latitude, point.longitude, dist.location.latitude, dist.location.longitude)
            if (distKm < minDistance) {
                minDistance = distKm
                bestPlace = districtToPlace(dist)
            }
        }

        // 2. Check major places
        places.forEach { place ->
            val distKm = calculateDistanceKm(point.latitude, point.longitude, place.location.latitude, place.location.longitude)
            if (distKm < minDistance) {
                minDistance = distKm
                bestPlace = place
            }
        }

        // 3. Check governorates
        EgyptMapData.ALL_EGYPT_GOVERNORATES.forEach { gov ->
            val distKm = calculateDistanceKm(point.latitude, point.longitude, gov.location.latitude, gov.location.longitude)
            if (distKm < minDistance) {
                minDistance = distKm
                bestPlace = governorateToPlace(gov)
            }
        }

        return bestPlace ?: places.first()
    }

    private fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}
