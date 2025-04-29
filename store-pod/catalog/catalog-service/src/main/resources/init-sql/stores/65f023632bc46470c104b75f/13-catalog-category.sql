/*
Generated SQL inserts for catalog.category and catalog.category_description
Store ID: '65f023632bc46470c104b75f'
Languages: ['en','fr']
Starting category_id: 37
Starting description_id: 73
Number of categories: 12
Domain: electronics
*/

-- Category 37: Computers & Tablets (Top Level)
INSERT INTO catalog.category (category_id, date_created, date_modified, category_image, category_status, code,
                              depth, featured, lineage, sort_order, visible, store_merchant_id, parent_id)
VALUES (37, '2024-04-02 17:00:00.000000', '2024-04-02 17:00:00.000000', 'computers-tablets.jpg', true,
        'COMPUTERS_TABLETS', 0,
        true, '/37/', 0, true, '65f023632bc46470c104b75f', null)
on conflict (category_id) do nothing;

INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (73, '2024-04-02 17:00:00.000000', '2024-04-02 17:00:00.000000',
        'Explore our wide range of laptops, desktops, and tablets.', 'Computers & Tablets',
        'Shop Computers, Laptops & Tablets Online', 'Latest Tech Deals',
        'Find the best deals on computers, laptops, and tablets from top brands. Shop now at USA Electronics Hub.',
        'computers, laptops, tablets, desktops, tech deals', 'Computers & Tablets | USA Electronics Hub',
        'computers-tablets', 'en', 37)
on conflict (description_id) do nothing;

INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (74, '2024-04-02 17:00:00.000000', '2024-04-02 17:00:00.000000',
        'Découvrez notre large gamme d''ordinateurs portables, de bureau et de tablettes.',
        'Ordinateurs et Tablettes', 'Achetez Ordinateurs, Portables et Tablettes en Ligne',
        'Dernières Offres Tech',
        'Trouvez les meilleures offres sur les ordinateurs, portables et tablettes des meilleures marques. Achetez maintenant chez USA Electronics Hub.',
        'ordinateurs, portables, tablettes, ordinateurs de bureau, offres tech',
        'Ordinateurs et Tablettes | USA Electronics Hub', 'ordinateurs-tablettes', 'fr', 37)
on conflict (description_id) do nothing;

-- Category 38: Mobile Phones & Accessories (Top Level)
INSERT INTO catalog.category (category_id, date_created, date_modified, category_image, category_status, code,
                              depth, featured, lineage, sort_order, visible, store_merchant_id, parent_id)
VALUES (38, '2024-04-02 17:00:00.000000', '2024-04-02 17:00:00.000000', 'mobile-phones.jpg', true,
        'MOBILE_PHONES_ACC', 0,
        true, '/38/', 1, true, '65f023632bc46470c104b75f', null)
on conflict (category_id) do nothing;

INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (75, '2024-04-02 17:00:00.000000', '2024-04-02 17:00:00.000000',
        'Find the latest smartphones, smartwatches, and mobile accessories.', 'Mobile Phones & Accessories',
        'Smartphones, Smartwatches & Accessories', 'Stay Connected',
        'Shop the newest smartphones, smartwatches, cases, chargers, and more. Stay connected with USA Electronics Hub.',
        'smartphones, mobile phones, accessories, cases, chargers, smartwatches',
        'Mobile Phones & Accessories | USA Electronics Hub', 'mobile-phones-accessories', 'en', 38)
on conflict (description_id) do nothing;

INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (76, '2024-04-02 17:00:00.000000', '2024-04-02 17:00:00.000000',
        'Trouvez les derniers smartphones, montres connectées et accessoires mobiles.',
        'Téléphones Mobiles et Accessoires', 'Smartphones, Montres Connectées et Accessoires', 'Restez Connecté',
        'Achetez les plus récents smartphones, montres connectées, étuis, chargeurs et plus encore. Restez connecté avec USA Electronics Hub.',
        'smartphones, téléphones mobiles, accessoires, étuis, chargeurs, montres connectées',
        'Téléphones Mobiles et Accessoires | USA Electronics Hub', 'telephones-mobiles-accessoires', 'fr', 38)
on conflict (description_id) do nothing;

-- Category 39: Audio & Headphones (Top Level)
INSERT INTO catalog.category (category_id, date_created, date_modified, category_image, category_status, code,
                              depth, featured, lineage, sort_order, visible, store_merchant_id, parent_id)
VALUES (39, '2024-04-02 17:00:00.000000', '2024-04-02 17:00:00.000000', 'audio-headphones.jpg', true,
        'AUDIO_HEADPHONES', 0,
        false, '/39/', 2, true, '65f023632bc46470c104b75f', null)
on conflict (category_id) do nothing;

INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (77, '2024-04-02 17:00:00.000000', '2024-04-02 17:00:00.000000',
        'Immerse yourself in sound with our headphones, earbuds, and speakers.', 'Audio & Headphones',
        'Headphones, Earbuds & Speakers', 'Premium Sound Quality',
        'Discover high-quality headphones, true wireless earbuds, and portable speakers for every lifestyle.',
        'audio, headphones, earbuds, speakers, bluetooth, wireless', 'Audio & Headphones | USA Electronics Hub',
        'audio-headphones', 'en', 39)
on conflict (description_id) do nothing;

INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (78, '2024-04-02 17:00:00.000000', '2024-04-02 17:00:00.000000',
        'Plongez dans le son avec nos casques, écouteurs et haut-parleurs.', 'Audio et Casques',
        'Casques, Écouteurs et Haut-parleurs', 'Qualité Sonore Premium',
        'Découvrez des casques de haute qualité, des écouteurs sans fil et des haut-parleurs portables pour tous les styles de vie.',
        'audio, casques, écouteurs, haut-parleurs, bluetooth, sans fil', 'Audio et Casques | USA Electronics Hub',
        'audio-casques', 'fr', 39)
on conflict (description_id) do nothing;

-- Category 40: TV & Home Theater (Top Level)
INSERT INTO catalog.category (category_id, date_created, date_modified, category_image, category_status, code,
                              depth, featured, lineage, sort_order, visible, store_merchant_id, parent_id)
VALUES (40, '2024-04-02 17:00:00.000000', '2024-04-02 17:00:00.000000', 'tv-home-theater.jpg', true,
        'TV_HOME_THEATER', 0,
        true, '/40/', 3, true, '65f023632bc46470c104b75f', null)
on conflict (category_id) do nothing;

INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (79, '2024-04-02 17:00:00.000000', '2024-04-02 17:00:00.000000',
        'Upgrade your viewing experience with stunning TVs and home theater systems.', 'TV & Home Theater',
        'Shop TVs, Projectors & Soundbars', 'Cinema Experience at Home',
        'Find the latest 4K, 8K, OLED TVs, projectors, soundbars, and home audio systems.',
        'tv, television, home theater, soundbar, projector, 4k, oled', 'TV & Home Theater | USA Electronics Hub',
        'tv-home-theater', 'en', 40)
on conflict (description_id) do nothing;

INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (80, '2024-04-02 17:00:00.000000', '2024-04-02 17:00:00.000000',
        'Améliorez votre expérience visuelle avec des téléviseurs et systèmes home cinéma époustouflants.',
        'TV et Home Cinéma', 'Achetez TV, Projecteurs et Barres de Son', 'Expérience Cinéma à Domicile',
        'Trouvez les derniers téléviseurs 4K, 8K, OLED, projecteurs, barres de son et systèmes audio domestiques.',
        'tv, télévision, home cinéma, barre de son, projecteur, 4k, oled', 'TV et Home Cinéma | USA Electronics Hub',
        'tv-home-cinema', 'fr', 40)
on conflict (description_id) do nothing;

-- Category 41: Cameras & Drones (Top Level)
INSERT INTO catalog.category (category_id, date_created, date_modified, category_image, category_status, code,
                              depth, featured, lineage, sort_order, visible, store_merchant_id, parent_id)
VALUES (41, '2024-04-02 17:00:00.000000', '2024-04-02 17:00:00.000000', 'cameras-drones.jpg', true,
        'CAMERAS_DRONES', 0,
        false, '/41/', 4, true, '65f023632bc46470c104b75f', null)
on conflict (category_id) do nothing;

INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (81, '2024-04-02 17:00:00.000000', '2024-04-02 17:00:00.000000',
        'Capture life''s moments with high-quality cameras, drones, and accessories.', 'Cameras & Drones',
        'Digital Cameras, Drones & Accessories', 'Capture Every Angle',
        'Shop DSLR, mirrorless, action cameras, drones, gimbals, and photography accessories.',
        'cameras, drones, photography, dslr, mirrorless, action camera, gimbal',
        'Cameras & Drones | USA Electronics Hub', 'cameras-drones', 'en', 41)
on conflict (description_id) do nothing;

INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (82, '2024-04-02 17:00:00.000000', '2024-04-02 17:00:00.000000',
        'Capturez les moments de la vie avec des appareils photo, drones et accessoires de haute qualité.',
        'Appareils Photo et Drones', 'Appareils Photo Numériques, Drones et Accessoires', 'Capturez Chaque Angle',
        'Achetez des appareils photo reflex numériques, hybrides, caméras d''action, drones, stabilisateurs et accessoires de photographie.',
        'appareils photo, drones, photographie, reflex, hybride, caméra action, stabilisateur',
        'Appareils Photo et Drones | USA Electronics Hub', 'appareils-photo-drones', 'fr', 41)
on conflict (description_id) do nothing;

-- Category 42: Gaming (Top Level)
INSERT INTO catalog.category (category_id, date_created, date_modified, category_image, category_status, code,
                              depth, featured, lineage, sort_order, visible, store_merchant_id, parent_id)
VALUES (42, '2024-04-02 17:00:00.000000', '2024-04-02 17:00:00.000000', 'gaming.jpg', true, 'GAMING', 0,
        true, '/42/', 5, true, '65f023632bc46470c104b75f', null)
on conflict (category_id) do nothing;

INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (83, '2024-04-02 17:00:00.000000', '2024-04-02 17:00:00.000000',
        'Level up your gaming setup with consoles, PCs, accessories, and games.', 'Gaming',
        'Gaming Consoles, PCs, Accessories & Games', 'Ultimate Gaming Gear',
        'Find everything you need for gaming: consoles, gaming laptops, monitors, headsets, controllers, and the latest games.',
        'gaming, consoles, pc gaming, accessories, games, playstation, xbox, nintendo',
        'Gaming | USA Electronics Hub', 'gaming', 'en', 42)
on conflict (description_id) do nothing;

INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (84, '2024-04-02 17:00:00.000000', '2024-04-02 17:00:00.000000',
        'Améliorez votre configuration de jeu avec des consoles, PC, accessoires et jeux.', 'Jeux Vidéo',
        'Consoles de Jeux, PC, Accessoires et Jeux', 'Équipement de Jeu Ultime',
        'Trouvez tout ce dont vous avez besoin pour jouer : consoles, PC portables de jeu, moniteurs, casques, manettes et les derniers jeux.',
        'jeux vidéo, consoles, jeu pc, accessoires, jeux, playstation, xbox, nintendo',
        'Jeux Vidéo | USA Electronics Hub', 'jeux-video', 'fr', 42)
on conflict (description_id) do nothing;

-- Category 43: Laptops (Sub-category of 37)
INSERT INTO catalog.category (category_id, date_created, date_modified, category_image, category_status, code,
                              depth, featured, lineage, sort_order, visible, store_merchant_id, parent_id)
VALUES (43, '2024-04-02 17:00:00.000000', '2024-04-02 17:00:00.000000', 'laptops.jpg', true, 'LAPTOPS', 1,
        true, '/37/43/', 0, true, '65f023632bc46470c104b75f', 37)
on conflict (category_id) do nothing;

INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (85, '2024-04-02 17:00:00.000000', '2024-04-02 17:00:00.000000',
        'Powerful and portable laptops for work, school, and entertainment.', 'Laptops',
        'Shop Laptops - Windows, MacBooks & Chromebooks', 'Portable Power',
        'Choose from a wide selection of laptops including MacBooks, Windows laptops, and Chromebooks.',
        'laptops, notebooks, macbook, windows laptop, chromebook', 'Laptops | USA Electronics Hub', 'laptops', 'en',
        43)
on conflict (description_id) do nothing;

INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (86, '2024-04-02 17:00:00.000000', '2024-04-02 17:00:00.000000',
        'Ordinateurs portables puissants et portables pour le travail, l''école et le divertissement.',
        'Ordinateurs Portables', 'Achetez des Portables - Windows, MacBooks et Chromebooks', 'Puissance Portable',
        'Choisissez parmi une large sélection d''ordinateurs portables, y compris les MacBooks, les portables Windows et les Chromebooks.',
        'ordinateurs portables, notebooks, macbook, portable windows, chromebook',
        'Ordinateurs Portables | USA Electronics Hub', 'ordinateurs-portables', 'fr', 43)
on conflict (description_id) do nothing;

-- Category 44: Smartphones (Sub-category of 38)
INSERT INTO catalog.category (category_id, date_created, date_modified, category_image, category_status, code,
                              depth, featured, lineage, sort_order, visible, store_merchant_id, parent_id)
VALUES (44, '2024-04-02 17:00:00.000000', '2024-04-02 17:00:00.000000', 'smartphones.jpg', true, 'SMARTPHONES', 1,
        true, '/38/44/', 0, true, '65f023632bc46470c104b75f', 38)
on conflict (category_id) do nothing;

INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (87, '2024-04-02 17:00:00.000000', '2024-04-02 17:00:00.000000',
        'Get the latest smartphones from Apple, Samsung, Google, and more.', 'Smartphones',
        'Latest Smartphones - iPhone, Android', 'Top Mobile Tech',
        'Find your next smartphone. Compare the latest iPhone and Android models.',
        'smartphones, iphone, android, samsung galaxy, google pixel', 'Smartphones | USA Electronics Hub',
        'smartphones', 'en', 44)
on conflict (description_id) do nothing;

INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (88, '2024-04-02 17:00:00.000000', '2024-04-02 17:00:00.000000',
        'Obtenez les derniers smartphones d''Apple, Samsung, Google et plus encore.', 'Smartphones',
        'Derniers Smartphones - iPhone, Android', 'Top Tech Mobile',
        'Trouvez votre prochain smartphone. Comparez les derniers modèles iPhone et Android.',
        'smartphones, iphone, android, samsung galaxy, google pixel', 'Smartphones | USA Electronics Hub',
        'smartphones', 'fr', 44)
on conflict (description_id) do nothing;

-- Category 45: Headphones (Sub-category of 39)
INSERT INTO catalog.category (category_id, date_created, date_modified, category_image, category_status, code,
                              depth, featured, lineage, sort_order, visible, store_merchant_id, parent_id)
VALUES (45, '2024-04-02 17:00:00.000000', '2024-04-02 17:00:00.000000', 'headphones-sub.jpg', true, 'HEADPHONES', 1,
        true, '/39/45/', 0, true, '65f023632bc46470c104b75f', 39)
on conflict (category_id) do nothing;

INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (89, '2024-04-02 17:00:00.000000', '2024-04-02 17:00:00.000000',
        'Find the perfect pair: over-ear, on-ear, earbuds, noise-cancelling, and wireless headphones.', 'Headphones',
        'Shop Headphones - Wireless, Noise-Cancelling & More', 'Your Perfect Sound',
        'Explore a wide range of headphones from top audio brands like Sony, Bose, Apple, and Beats.',
        'headphones, earbuds, noise cancelling, wireless headphones, bluetooth headphones',
        'Headphones | USA Electronics Hub', 'headphones', 'en', 45)
on conflict (description_id) do nothing;

INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (90, '2024-04-02 17:00:00.000000', '2024-04-02 17:00:00.000000',
        'Trouvez la paire parfaite : casques circum-auriculaires, supra-auriculaires, écouteurs, réduction de bruit et sans fil.',
        'Casques', 'Achetez des Casques - Sans Fil, Réduction de Bruit et Plus', 'Votre Son Parfait',
        'Explorez une large gamme de casques des meilleures marques audio comme Sony, Bose, Apple et Beats.',
        'casques, écouteurs, réduction de bruit, casques sans fil, casques bluetooth',
        'Casques | USA Electronics Hub', 'casques', 'fr', 45)
on conflict (description_id) do nothing;

-- Category 46: Televisions (Sub-category of 40)
INSERT INTO catalog.category (category_id, date_created, date_modified, category_image, category_status, code,
                              depth, featured, lineage, sort_order, visible, store_merchant_id, parent_id)
VALUES (46, '2024-04-02 17:00:00.000000', '2024-04-02 17:00:00.000000', 'televisions.jpg', true, 'TELEVISIONS', 1,
        true, '/40/46/', 0, true, '65f023632bc46470c104b75f', 40)
on conflict (category_id) do nothing;

INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (91, '2024-04-02 17:00:00.000000', '2024-04-02 17:00:00.000000',
        'Discover stunning picture quality with our range of 4K, 8K, OLED, and QLED TVs.', 'Televisions',
        'Shop Televisions - 4K, 8K, OLED, QLED', 'Incredible Picture Quality',
        'Find the perfect TV for your home from Samsung, LG, Sony, and other leading brands.',
        'tv, television, 4k tv, 8k tv, oled tv, qled tv, smart tv', 'Televisions | USA Electronics Hub', 'televisions',
        'en', 46)
on conflict (description_id) do nothing;

INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (92, '2024-04-02 17:00:00.000000', '2024-04-02 17:00:00.000000',
        'Découvrez une qualité d''image époustouflante avec notre gamme de téléviseurs 4K, 8K, OLED et QLED.',
        'Téléviseurs', 'Achetez des Téléviseurs - 4K, 8K, OLED, QLED', 'Qualité d''Image Incroyable',
        'Trouvez le téléviseur parfait pour votre maison parmi Samsung, LG, Sony et d''autres grandes marques.',
        'tv, télévision, tv 4k, tv 8k, tv oled, tv qled, smart tv', 'Téléviseurs | USA Electronics Hub', 'televiseurs',
        'fr', 46)
on conflict (description_id) do nothing;

-- Category 47: Gaming Consoles (Sub-category of 42)
INSERT INTO catalog.category (category_id, date_created, date_modified, category_image, category_status, code,
                              depth, featured, lineage, sort_order, visible, store_merchant_id, parent_id)
VALUES (47, '2024-04-02 17:00:00.000000', '2024-04-02 17:00:00.000000', 'gaming-consoles.jpg', true,
        'GAMING_CONSOLES', 1,
        true, '/42/47/', 0, true, '65f023632bc46470c104b75f', 42)
on conflict (category_id) do nothing;

INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (93, '2024-04-02 17:00:00.000000', '2024-04-02 17:00:00.000000',
        'Get the latest PlayStation, Xbox, and Nintendo gaming consoles and bundles.', 'Gaming Consoles',
        'PlayStation, Xbox & Nintendo Consoles', 'Next-Gen Gaming',
        'Shop the newest gaming consoles like PlayStation 5, Xbox Series X|S, and Nintendo Switch.',
        'gaming consoles, playstation 5, ps5, xbox series x, xbox series s, nintendo switch',
        'Gaming Consoles | USA Electronics Hub', 'gaming-consoles', 'en', 47)
on conflict (description_id) do nothing;

INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (94, '2024-04-02 17:00:00.000000', '2024-04-02 17:00:00.000000',
        'Obtenez les dernières consoles de jeux PlayStation, Xbox et Nintendo et leurs packs.', 'Consoles de Jeux',
        'Consoles PlayStation, Xbox et Nintendo', 'Jeu Nouvelle Génération',
        'Achetez les consoles de jeux les plus récentes comme la PlayStation 5, Xbox Series X|S et Nintendo Switch.',
        'consoles de jeux, playstation 5, ps5, xbox series x, xbox series s, nintendo switch',
        'Consoles de Jeux | USA Electronics Hub', 'consoles-de-jeux', 'fr', 47)
on conflict (description_id) do nothing;

-- Category 48: Smartwatches (Sub-category of 38)
INSERT INTO catalog.category (category_id, date_created, date_modified, category_image, category_status, code,
                              depth, featured, lineage, sort_order, visible, store_merchant_id, parent_id)
VALUES (48, '2024-04-02 17:00:00.000000', '2024-04-02 17:00:00.000000', 'smartwatches.jpg', true, 'SMARTWATCHES', 1,
        false, '/38/48/', 1, true, '65f023632bc46470c104b75f', 38)
on conflict (category_id) do nothing;

INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (95, '2024-04-02 17:00:00.000000', '2024-04-02 17:00:00.000000',
        'Track your fitness, stay connected, and more with the latest smartwatches.', 'Smartwatches',
        'Shop Smartwatches - Apple Watch, Galaxy Watch & More', 'Wearable Tech',
        'Explore top smartwatches like Apple Watch, Samsung Galaxy Watch, Fitbit, and Garmin.',
        'smartwatch, apple watch, galaxy watch, fitbit, garmin, wearable tech',
        'Smartwatches | USA Electronics Hub', 'smartwatches', 'en', 48)
on conflict (description_id) do nothing;

INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (96, '2024-04-02 17:00:00.000000', '2024-04-02 17:00:00.000000',
        'Suivez votre forme physique, restez connecté et plus encore avec les dernières montres connectées.',
        'Montres Connectées', 'Achetez des Montres Connectées - Apple Watch, Galaxy Watch et Plus',
        'Technologie Portable',
        'Explorez les meilleures montres connectées comme Apple Watch, Samsung Galaxy Watch, Fitbit et Garmin.',
        'montre connectée, apple watch, galaxy watch, fitbit, garmin, technologie portable',
        'Montres Connectées | USA Electronics Hub', 'montres-connectees', 'fr', 48)
on conflict (description_id) do nothing;