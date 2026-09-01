/*
Generated SQL inserts for catalog.product and catalog.product_description based on the original template.
Store ID: '65f023632bc46470c104b75f'
Languages: ['en','fr']
Manufacturer IDs: (19, 20, 21, 22, 23, 24)
Product Type IDs: (13, 14, 15, 16)
Starting product_id: 136
Starting description_id: 271
Number of products: 15
Domain: electronics
*/

-- Product 136: Apple iPhone 15 Pro (Man: 19, Type: 13)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (136, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        5.77, false, 2.78, true, false, 0.41, 0.32, 'ELEC-REF-136', 0, 19,
        '65f023632bc46470c104b75f', 13) -- Type 13 (SMARTPHONES)
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (271, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Apple iPhone 15 Pro</h1><p>Experience the ultimate iPhone. Forged in titanium and featuring the groundbreaking A17 Pro chip, a customizable Action button, and the most powerful iPhone camera system ever.</p><h2>Key Features:</h2><ul><li>Titanium design</li><li>Super Retina XDR display with ProMotion</li><li>A17 Pro chip</li><li>Pro camera system (48MP Main, Ultra Wide, Telephoto)</li><li>Action button</li><li>USB-C connector</li></ul><h2>Specifications:</h2><ul><li>Display: 6.1-inch Super Retina XDR</li><li>Processor: A17 Pro</li><li>Storage Options: 128GB, 256GB, 512GB, 1TB</li><li>Water Resistance: IP68</li></ul>',
        'Apple iPhone 15 Pro', 'Apple iPhone 15 Pro - Titanium',
        'The Apple iPhone 15 Pro features a strong and light titanium design, A17 Pro chip, Action button, and a powerful Pro camera system.',
        'iphone 15 pro, apple, smartphone, titanium, a17 pro, pro camera', 'Apple iPhone 15 Pro | Titanium Powerhouse',
        'Titanium design, A17 Pro chip', 'apple-iphone-15-pro', 'en', 136)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (272, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Apple iPhone 15 Pro</h1><p>Découvrez l''iPhone ultime. Forgé en titane et doté de la puce révolutionnaire A17 Pro, d''un bouton Action personnalisable et du système photo pour iPhone le plus puissant jamais conçu.</p><h2>Caractéristiques Clés :</h2><ul><li>Design en titane</li><li>Écran Super Retina XDR avec ProMotion</li><li>Puce A17 Pro</li><li>Système photo pro (Principal 48 Mpx, Ultra grand-angle, Téléobjectif)</li><li>Bouton Action</li><li>Connecteur USB-C</li></ul><h2>Spécifications :</h2><ul><li>Écran : Super Retina XDR 6,1 pouces</li><li>Processeur : A17 Pro</li><li>Options de stockage : 128 Go, 256 Go, 512 Go, 1 To</li><li>Résistance à l''eau : IP68</li></ul>',
        'Apple iPhone 15 Pro', 'Apple iPhone 15 Pro - Titane',
        'L''Apple iPhone 15 Pro est doté d''un design en titane solide et léger, de la puce A17 Pro, du bouton Action et d''un puissant système photo Pro.',
        'iphone 15 pro, apple, smartphone, titane, a17 pro, caméra pro', 'Apple iPhone 15 Pro | Puissance en Titane',
        'Design en titane, puce A17 Pro', 'apple-iphone-15-pro', 'fr', 136)
on conflict (description_id) do nothing;

-- Product 137: Samsung Galaxy S24 Ultra (Man: 20, Type: 13)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (137, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        6.40, false, 3.11, true, false, 0.51, 0.34, 'ELEC-REF-137', 1, 20,
        '65f023632bc46470c104b75f', 13) -- Type 13 (SMARTPHONES)
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (273, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Samsung Galaxy S24 Ultra</h1><p>Meet the Galaxy S24 Ultra, the ultimate form of Galaxy Ultra with a new titanium exterior and a 6.8" flat display. It''s an absolute marvel of design.</p><h2>Key Features:</h2><ul><li>Titanium frame</li><li>Built-in S Pen</li><li>200MP Wide-angle Camera</li><li>Galaxy AI features</li><li>Snapdragon® 8 Gen 3 for Galaxy</li><li>Dynamic AMOLED 2X display</li></ul><h2>Specifications:</h2><ul><li>Display: 6.8-inch Dynamic AMOLED 2X QHD+</li><li>Processor: Snapdragon® 8 Gen 3 for Galaxy</li><li>RAM: 12GB</li><li>Storage Options: 256GB, 512GB, 1TB</li><li>Battery: 5000mAh</li></ul>',
        'Samsung Galaxy S24 Ultra', 'Samsung Galaxy S24 Ultra with Galaxy AI',
        'The Samsung Galaxy S24 Ultra features a titanium frame, built-in S Pen, 200MP camera, and powerful Galaxy AI capabilities.',
        'galaxy s24 ultra, samsung, smartphone, android, titanium, s pen, galaxy ai, 200mp camera',
        'Samsung Galaxy S24 Ultra | AI Phone',
        'Titanium frame, Galaxy AI', 'samsung-galaxy-s24-ultra', 'en', 137)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (274, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Samsung Galaxy S24 Ultra</h1><p>Découvrez le Galaxy S24 Ultra, la forme ultime du Galaxy Ultra avec un nouvel extérieur en titane et un écran plat de 6,8". C''est une merveille absolue de design.</p><h2>Caractéristiques Clés :</h2><ul><li>Cadre en titane</li><li>S Pen intégré</li><li>Appareil photo grand angle 200 Mpx</li><li>Fonctionnalités Galaxy AI</li><li>Snapdragon® 8 Gen 3 pour Galaxy</li><li>Écran Dynamic AMOLED 2X</li></ul><h2>Spécifications :</h2><ul><li>Écran : Dynamic AMOLED 2X QHD+ 6,8 pouces</li><li>Processeur : Snapdragon® 8 Gen 3 pour Galaxy</li><li>RAM : 12 Go</li><li>Options de stockage : 256 Go, 512 Go, 1 To</li><li>Batterie : 5000 mAh</li></ul>',
        'Samsung Galaxy S24 Ultra', 'Samsung Galaxy S24 Ultra avec Galaxy AI',
        'Le Samsung Galaxy S24 Ultra est doté d''un cadre en titane, d''un S Pen intégré, d''un appareil photo 200 Mpx et de puissantes capacités Galaxy AI.',
        'galaxy s24 ultra, samsung, smartphone, android, titane, s pen, galaxy ai, caméra 200mp',
        'Samsung Galaxy S24 Ultra | Téléphone IA',
        'Cadre en titane, Galaxy AI', 'samsung-galaxy-s24-ultra', 'fr', 137)
on conflict (description_id) do nothing;

-- Product 138: Dell XPS 15 Laptop (Man: 23, Type: 14)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (138, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        0.71, false, 9.06, true, false, 4.21, 13.57, 'ELEC-REF-138', 2, 23,
        '65f023632bc46470c104b75f', 14) -- Type 14 (LAPTOPS)
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (275, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Dell XPS 15 Laptop</h1><p>Stunning display, immersive sound and powerful performance come together in the XPS 15 laptop. Featuring the latest Intel® Core™ processors and optional NVIDIA® graphics.</p><h2>Key Features:</h2><ul><li>15.6-inch InfinityEdge display (OLED option available)</li><li>Latest Intel® Core™ Ultra processors</li><li>Optional NVIDIA® GeForce RTX™ 40 Series graphics</li><li>Premium machined aluminum design</li><li>Large touchpad and comfortable keyboard</li><li>Windows 11</li></ul><h2>Specifications:</h2><ul><li>Display: 15.6" FHD+ or 3.5K OLED</li><li>Processor: Intel® Core™ Ultra 7 / Ultra 9</li><li>RAM: 16GB / 32GB / 64GB DDR5</li><li>Storage: 512GB / 1TB / 2TB / 4TB NVMe SSD</li></ul>',
        'Dell XPS 15 Laptop', 'Dell XPS 15 - Performance & Style',
        'The Dell XPS 15 laptop offers a stunning InfinityEdge display, powerful Intel Core Ultra processors, and optional NVIDIA graphics in a premium design.',
        'dell xps 15, laptop, windows 11, intel core ultra, nvidia rtx, oled',
        'Dell XPS 15 Laptop | Premium Performance',
        'InfinityEdge display, Intel Core Ultra', 'dell-xps-15-laptop', 'en', 138)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (276, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Ordinateur Portable Dell XPS 15</h1><p>Un écran époustouflant, un son immersif et des performances puissantes réunis dans l''ordinateur portable XPS 15. Doté des derniers processeurs Intel® Core™ et de cartes graphiques NVIDIA® en option.</p><h2>Caractéristiques Clés :</h2><ul><li>Écran InfinityEdge 15,6 pouces (option OLED disponible)</li><li>Derniers processeurs Intel® Core™ Ultra</li><li>Cartes graphiques NVIDIA® GeForce RTX™ série 40 en option</li><li>Design premium en aluminium usiné</li><li>Grand pavé tactile et clavier confortable</li><li>Windows 11</li></ul><h2>Spécifications :</h2><ul><li>Écran : 15,6" FHD+ ou 3.5K OLED</li><li>Processeur : Intel® Core™ Ultra 7 / Ultra 9</li><li>RAM : 16 Go / 32 Go / 64 Go DDR5</li><li>Stockage : 512 Go / 1 To / 2 To / 4 To SSD NVMe</li></ul>',
        'Ordinateur Portable Dell XPS 15', 'Dell XPS 15 - Performance et Style',
        'L''ordinateur portable Dell XPS 15 offre un écran InfinityEdge époustouflant, de puissants processeurs Intel Core Ultra et des graphiques NVIDIA en option dans un design premium.',
        'dell xps 15, ordinateur portable, windows 11, intel core ultra, nvidia rtx, oled',
        'Ordinateur Portable Dell XPS 15 | Performance Premium',
        'Écran InfinityEdge, Intel Core Ultra', 'dell-xps-15-portable', 'fr', 138)
on conflict (description_id) do nothing;

-- Product 139: Sony WH-1000XM5 Headphones (Man: 21, Type: 15)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (139, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        null, false, null, true, false, 0.55, null, 'ELEC-REF-139', 3, 21,
        '65f023632bc46470c104b75f', 15) -- Type 15 (HEADPHONES)
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (277, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Sony WH-1000XM5 Wireless Noise Cancelling Headphones</h1><p>Experience industry-leading noise cancellation and exceptional sound quality with the Sony WH-1000XM5 headphones. Lightweight design and long battery life for all-day comfort.</p><h2>Key Features:</h2><ul><li>Industry-leading noise cancellation with Integrated Processor V1</li><li>Magnificent sound engineered to perfection</li><li>Crystal clear hands-free calling</li><li>Up to 30-hour battery life with quick charging</li><li>Ultra-comfortable, lightweight design</li><li>Multipoint connection</li></ul><h2>Specifications:</h2><ul><li>Driver Unit: 30mm</li><li>Frequency Response: 4 Hz-40,000 Hz (JEITA)</li><li>Battery Life: Max. 30 hours (NC ON), Max. 40 hours (NC OFF)</li><li>Bluetooth Version: 5.2</li></ul>',
        'Sony WH-1000XM5 Headphones', 'Sony WH-1000XM5 Wireless Noise Cancelling Headphones',
        'Sony WH-1000XM5 offers the best noise cancelling performance, superb sound quality, and comfortable lightweight design.',
        'sony wh-1000xm5, headphones, noise cancelling, wireless, bluetooth, sony headphones',
        'Sony WH-1000XM5 | Best Noise Cancelling Headphones',
        'Industry-leading noise cancellation', 'sony-wh-1000xm5-headphones', 'en', 139)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (278, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Casque sans Fil à Réduction de Bruit Sony WH-1000XM5</h1><p>Profitez de la meilleure réduction de bruit du marché et d''une qualité sonore exceptionnelle avec le casque Sony WH-1000XM5. Conception légère et longue autonomie pour un confort toute la journée.</p><h2>Caractéristiques Clés :</h2><ul><li>Meilleure réduction de bruit du marché avec le processeur intégré V1</li><li>Son magnifique conçu à la perfection</li><li>Appels mains libres d''une clarté cristalline</li><li>Jusqu''à 30 heures d''autonomie avec charge rapide</li><li>Design ultra-confortable et léger</li><li>Connexion multipoint</li></ul><h2>Spécifications :</h2><ul><li>Diaphragme : 30 mm</li><li>Réponse en fréquence : 4 Hz-40 000 Hz (JEITA)</li><li>Autonomie : Max. 30 heures (NC ON), Max. 40 heures (NC OFF)</li><li>Version Bluetooth : 5.2</li></ul>',
        'Casque Sony WH-1000XM5', 'Casque sans Fil à Réduction de Bruit Sony WH-1000XM5',
        'Le Sony WH-1000XM5 offre les meilleures performances de réduction de bruit, une superbe qualité sonore et un design léger et confortable.',
        'sony wh-1000xm5, casque, réduction de bruit, sans fil, bluetooth, casque sony',
        'Sony WH-1000XM5 | Meilleur Casque à Réduction de Bruit',
        'Meilleure réduction de bruit du marché', 'sony-wh-1000xm5-casque', 'fr', 139)
on conflict (description_id) do nothing;

-- Product 140: LG C3 65-inch OLED TV (Man: 22, Type: 16)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (140, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        32.5, false, 1.8, true, false, 35.9, 56.7, 'ELEC-REF-140', 4, 22,
        '65f023632bc46470c104b75f', 16) -- Type 16 (TELEVISIONS)
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (279, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>LG C3 65-inch evo OLED 4K Smart TV</h1><p>Experience the brilliance of LG OLED evo. The C3 Series features the α9 AI Processor Gen6 for stunning picture and sound, plus Brightness Booster for luminous picture quality.</p><h2>Key Features:</h2><ul><li>LG OLED evo technology</li><li>α9 AI Processor Gen6</li><li>Brightness Booster</li><li>Dolby Vision & Dolby Atmos</li><li>webOS 23 Smart TV platform</li><li>Ultimate Gaming: G-SYNC, FreeSync Premium, VRR</li></ul><h2>Specifications:</h2><ul><li>Screen Size: 65 inches</li><li>Resolution: 4K Ultra HD (3840 x 2160)</li><li>Display Type: OLED evo</li><li>Refresh Rate: 120Hz Native</li><li>Smart TV Platform: webOS 23</li></ul>',
        'LG C3 65-inch OLED TV', 'LG C3 65" Class 4K UHD OLED WebOS 23 Smart TV',
        'The LG C3 65-inch OLED evo TV delivers stunning 4K picture quality, enhanced brightness, and smart features powered by the α9 AI Processor Gen6.',
        'lg c3, oled tv, 65 inch tv, 4k tv, smart tv, webos, dolby vision, gaming tv',
        'LG C3 65-inch OLED evo 4K TV | Brilliant Picture',
        'OLED evo, α9 AI Processor Gen6', 'lg-c3-65-inch-oled-tv', 'en', 140)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (280, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Téléviseur Intelligent LG C3 65 pouces evo OLED 4K</h1><p>Découvrez la brillance de LG OLED evo. La série C3 est dotée du processeur α9 AI Gen6 pour une image et un son époustouflants, ainsi que du Brightness Booster pour une qualité d''image lumineuse.</p><h2>Caractéristiques Clés :</h2><ul><li>Technologie LG OLED evo</li><li>Processeur α9 AI Gen6</li><li>Brightness Booster</li><li>Dolby Vision & Dolby Atmos</li><li>Plateforme Smart TV webOS 23</li><li>Jeu Ultime : G-SYNC, FreeSync Premium, VRR</li></ul><h2>Spécifications :</h2><ul><li>Taille de l''écran : 65 pouces</li><li>Résolution : 4K Ultra HD (3840 x 2160)</li><li>Type d''affichage : OLED evo</li><li>Taux de rafraîchissement : 120Hz Natif</li><li>Plateforme Smart TV : webOS 23</li></ul>',
        'Téléviseur LG C3 65 pouces OLED', 'Téléviseur Intelligent LG C3 65" Classe 4K UHD OLED WebOS 23',
        'Le téléviseur LG C3 65 pouces OLED evo offre une qualité d''image 4K époustouflante, une luminosité améliorée et des fonctionnalités intelligentes grâce au processeur α9 AI Gen6.',
        'lg c3, tv oled, tv 65 pouces, tv 4k, smart tv, webos, dolby vision, tv gaming',
        'Téléviseur LG C3 65 pouces OLED evo 4K | Image Brillante',
        'OLED evo, Processeur α9 AI Gen6', 'lg-c3-65-pouces-oled-tv', 'fr', 140)
on conflict (description_id) do nothing;

-- Product 141: Apple MacBook Air M3 (Man: 19, Type: 14)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (141, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        0.44, false, 8.46, true, false, 2.7, 11.97, 'ELEC-REF-141', 5, 19,
        '65f023632bc46470c104b75f', 14) -- Type 14 (LAPTOPS)
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (281, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Apple MacBook Air 13-inch with M3 chip</h1><p>Supercharged by the M3 chip, the 13-inch MacBook Air is strikingly thin and light, delivering incredible performance and up to 18 hours of battery life.</p><h2>Key Features:</h2><ul><li>Apple M3 chip</li><li>13.6-inch Liquid Retina display</li><li>Up to 18 hours of battery life</li><li>Thin and light design</li><li>Fanless design for silent operation</li><li>1080p FaceTime HD camera</li><li>macOS</li></ul><h2>Specifications:</h2><ul><li>Chip: Apple M3 (8-core CPU, 8-core or 10-core GPU)</li><li>Memory: 8GB / 16GB / 24GB unified memory</li><li>Storage: 256GB / 512GB / 1TB / 2TB SSD</li><li>Display: 13.6-inch Liquid Retina</li></ul>',
        'Apple MacBook Air 13-inch M3', 'Apple MacBook Air 13" with M3 Chip',
        'The incredibly thin and light MacBook Air is now supercharged by the Apple M3 chip for amazing performance and battery life.',
        'macbook air, m3 chip, apple, laptop, macos, liquid retina',
        'Apple MacBook Air 13-inch M3 | Supercharged Performance',
        'M3 chip, Up to 18 hours battery', 'apple-macbook-air-13-m3', 'en', 141)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (282, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Apple MacBook Air 13 pouces avec puce M3</h1><p>Survitaminé par la puce M3, le MacBook Air 13 pouces est remarquablement fin et léger, offrant des performances incroyables et jusqu''à 18 heures d''autonomie.</p><h2>Caractéristiques Clés :</h2><ul><li>Puce Apple M3</li><li>Écran Liquid Retina 13,6 pouces</li><li>Jusqu''à 18 heures d''autonomie</li><li>Design fin et léger</li><li>Conception sans ventilateur pour un fonctionnement silencieux</li><li>Caméra FaceTime HD 1080p</li><li>macOS</li></ul><h2>Spécifications :</h2><ul><li>Puce : Apple M3 (CPU 8 cœurs, GPU 8 ou 10 cœurs)</li><li>Mémoire : 8 Go / 16 Go / 24 Go de mémoire unifiée</li><li>Stockage : 256 Go / 512 Go / 1 To / 2 To SSD</li><li>Écran : Liquid Retina 13,6 pouces</li></ul>',
        'Apple MacBook Air 13 pouces M3', 'Apple MacBook Air 13" avec Puce M3',
        'L''incroyablement fin et léger MacBook Air est désormais suralimenté par la puce Apple M3 pour des performances et une autonomie étonnantes.',
        'macbook air, puce m3, apple, ordinateur portable, macos, liquid retina',
        'Apple MacBook Air 13 pouces M3 | Performances Survitaminées',
        'Puce M3, Jusqu''à 18h d''autonomie', 'apple-macbook-air-13-m3', 'fr', 141)
on conflict (description_id) do nothing;

-- Product 142: Samsung QN90C 55-inch QLED TV (Man: 20, Type: 16)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (142, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        27.9, false, 1.0, true, false, 34.0, 48.3, 'ELEC-REF-142', 6, 20,
        '65f023632bc46470c104b75f', 16) -- Type 16 (TELEVISIONS)
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (283, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Samsung 55" Class QN90C Neo QLED 4K Smart TV</h1><p>Experience brilliant details and dynamic contrast with Quantum Matrix Technology on the Samsung Neo QLED 4K QN90C. Powered by the Neural Quantum Processor 4K for AI-upscaled picture quality.</p><h2>Key Features:</h2><ul><li>Quantum Matrix Technology with Mini LEDs</li><li>Neural Quantum Processor 4K</li><li>Neo Quantum HDR+</li><li>Anti-Glare with Ultra Viewing Angle</li><li>Dolby Atmos and Object Tracking Sound+</li><li>Tizen Smart TV Hub</li></ul><h2>Specifications:</h2><ul><li>Screen Size: 55 inches</li><li>Resolution: 4K Ultra HD (3840 x 2160)</li><li>Display Type: Neo QLED (Mini LED)</li><li>Refresh Rate: 120Hz</li><li>Smart TV Platform: Tizen</li></ul>',
        'Samsung QN90C 55-inch Neo QLED TV', 'Samsung 55" Class QN90C Neo QLED 4K Smart TV (2023)',
        'The Samsung QN90C 55-inch Neo QLED TV features Quantum Matrix Technology, Neural Quantum Processor 4K, and immersive Dolby Atmos sound.',
        'samsung qn90c, neo qled, 55 inch tv, 4k tv, smart tv, tizen, mini led, quantum dot',
        'Samsung QN90C 55" Neo QLED 4K TV | Brilliant Detail',
        'Quantum Matrix Tech, Neural Quantum Processor', 'samsung-qn90c-55-inch-neo-qled-tv', 'en', 142)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (284, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Téléviseur Intelligent Samsung 55" Classe QN90C Neo QLED 4K</h1><p>Découvrez des détails brillants et un contraste dynamique avec la technologie Quantum Matrix sur le Samsung Neo QLED 4K QN90C. Alimenté par le Neural Quantum Processor 4K pour une qualité d''image améliorée par l''IA.</p><h2>Caractéristiques Clés :</h2><ul><li>Technologie Quantum Matrix avec Mini LED</li><li>Neural Quantum Processor 4K</li><li>Neo Quantum HDR+</li><li>Anti-reflet avec Ultra Viewing Angle</li><li>Dolby Atmos et Object Tracking Sound+</li><li>Hub Smart TV Tizen</li></ul><h2>Spécifications :</h2><ul><li>Taille de l''écran : 55 pouces</li><li>Résolution : 4K Ultra HD (3840 x 2160)</li><li>Type d''affichage : Neo QLED (Mini LED)</li><li>Taux de rafraîchissement : 120Hz</li><li>Plateforme Smart TV : Tizen</li></ul>',
        'Téléviseur Samsung QN90C 55 pouces Neo QLED',
        'Téléviseur Intelligent Samsung 55" Classe QN90C Neo QLED 4K (2023)',
        'Le téléviseur Samsung QN90C 55 pouces Neo QLED est doté de la technologie Quantum Matrix, du Neural Quantum Processor 4K et d''un son immersif Dolby Atmos.',
        'samsung qn90c, neo qled, tv 55 pouces, tv 4k, smart tv, tizen, mini led, quantum dot',
        'Téléviseur Samsung QN90C 55" Neo QLED 4K | Détail Brillant',
        'Tech Quantum Matrix, Neural Quantum Processor', 'samsung-qn90c-55-pouces-neo-qled-tv', 'fr', 142)
on conflict (description_id) do nothing;

-- Product 143: HP Spectre x360 14 (Man: 24, Type: 14)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (143, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        0.67, false, 8.68, true, false, 3.19, 12.35, 'ELEC-REF-143', 7, 24,
        '65f023632bc46470c104b75f', 14) -- Type 14 (LAPTOPS)
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (285, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>HP Spectre x360 14 2-in-1 Laptop</h1><p>Experience the power of AI in a stunning 2-in-1 design. The HP Spectre x360 14 features Intel® Core™ Ultra processors, a brilliant OLED display, and adaptive features for ultimate flexibility.</p><h2>Key Features:</h2><ul><li>Intel® Core™ Ultra processors with AI</li><li>14-inch OLED touchscreen display</li><li>360° hinge for versatile use (laptop, tablet, tent, stand)</li><li>Premium gem-cut design</li><li>Long battery life</li><li>Windows 11</li></ul><h2>Specifications:</h2><ul><li>Display: 14" 2.8K OLED Touchscreen</li><li>Processor: Intel® Core™ Ultra 5 / Ultra 7</li><li>RAM: 16GB / 32GB LPDDR5x</li><li>Storage: 512GB / 1TB / 2TB PCIe® NVMe™ SSD</li></ul>',
        'HP Spectre x360 14 Laptop', 'HP Spectre x360 14 2-in-1 Laptop with Intel Core Ultra',
        'The HP Spectre x360 14 is a premium 2-in-1 laptop featuring AI-powered Intel Core Ultra processors and a stunning OLED display.',
        'hp spectre x360 14, 2-in-1 laptop, convertible laptop, windows 11, intel core ultra, oled, touchscreen',
        'HP Spectre x360 14 | AI-Powered 2-in-1',
        'Intel Core Ultra, OLED Touchscreen', 'hp-spectre-x360-14-laptop', 'en', 143)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (286, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Ordinateur Portable 2-en-1 HP Spectre x360 14</h1><p>Découvrez la puissance de l''IA dans un superbe design 2-en-1. Le HP Spectre x360 14 est doté de processeurs Intel® Core™ Ultra, d''un écran OLED brillant et de fonctionnalités adaptatives pour une flexibilité ultime.</p><h2>Caractéristiques Clés :</h2><ul><li>Processeurs Intel® Core™ Ultra avec IA</li><li>Écran tactile OLED 14 pouces</li><li>Charnière à 360° pour une utilisation polyvalente (portable, tablette, tente, chevalet)</li><li>Design premium taille diamant</li><li>Longue autonomie</li><li>Windows 11</li></ul><h2>Spécifications :</h2><ul><li>Écran : Tactile OLED 14" 2.8K</li><li>Processeur : Intel® Core™ Ultra 5 / Ultra 7</li><li>RAM : 16 Go / 32 Go LPDDR5x</li><li>Stockage : 512 Go / 1 To / 2 To SSD PCIe® NVMe™</li></ul>',
        'Ordinateur Portable HP Spectre x360 14', 'Ordinateur Portable 2-en-1 HP Spectre x360 14 avec Intel Core Ultra',
        'Le HP Spectre x360 14 est un ordinateur portable 2-en-1 haut de gamme doté de processeurs Intel Core Ultra assistés par IA et d''un superbe écran OLED.',
        'hp spectre x360 14, portable 2-en-1, portable convertible, windows 11, intel core ultra, oled, écran tactile',
        'HP Spectre x360 14 | 2-en-1 Assisté par IA',
        'Intel Core Ultra, Écran Tactile OLED', 'hp-spectre-x360-14-portable', 'fr', 143)
on conflict (description_id) do nothing;

-- Product 144: Apple AirPods Pro (2nd Gen) (Man: 19, Type: 15)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (144, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        null, false, null, true, false, 0.01, null, 'ELEC-REF-144', 8, 19,
        '65f023632bc46470c104b75f', 15) -- Type 15 (HEADPHONES)
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (287, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Apple AirPods Pro (2nd generation) with MagSafe Charging Case (USB-C)</h1><p>AirPods Pro feature up to 2x more Active Noise Cancellation, plus Adaptive Transparency, and Personalized Spatial Audio with dynamic head tracking for immersive sound. Now with multiple ear tips (XS, S, M, L) and up to 6 hours of listening time.</p><h2>Key Features:</h2><ul><li>Up to 2x more Active Noise Cancellation</li><li>Adaptive Transparency</li><li>Personalized Spatial Audio</li><li>H2 Apple silicon chip</li><li>Sweat and water resistant (IPX4)</li><li>MagSafe Charging Case (USB-C) with speaker and lanyard loop</li></ul><h2>Specifications:</h2><ul><li>Audio Technology: Custom high-excursion Apple driver, Custom high dynamic range amplifier</li><li>Chip: H2 headphone chip, U1 chip in MagSafe Charging Case</li><li>Battery Life: Up to 6 hours (AirPods), Up to 30 hours (with case)</li></ul>',
        'Apple AirPods Pro (2nd generation)', 'Apple AirPods Pro (2nd generation) USB-C',
        'Experience superior sound with AirPods Pro (2nd gen) featuring enhanced Active Noise Cancellation, Adaptive Transparency, and Personalized Spatial Audio.',
        'airpods pro, apple, earbuds, wireless earbuds, noise cancelling, spatial audio, usb-c',
        'Apple AirPods Pro (2nd Gen) | Rebuilt Sound',
        '2x More Active Noise Cancellation', 'apple-airpods-pro-2nd-gen', 'en', 144)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (288, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Apple AirPods Pro (2ᵉ génération) avec Boîtier de Charge MagSafe (USB-C)</h1><p>Les AirPods Pro offrent jusqu''à 2x plus de Réduction active du bruit, ainsi que la Transparence adaptative et l''Audio spatial personnalisé avec suivi dynamique de la tête pour un son immersif. Maintenant avec plusieurs embouts (XS, S, M, L) et jusqu''à 6 heures d''écoute.</p><h2>Caractéristiques Clés :</h2><ul><li>Jusqu''à 2x plus de Réduction active du bruit</li><li>Transparence adaptative</li><li>Audio spatial personnalisé</li><li>Puce Apple H2</li><li>Résistance à la transpiration et à l''eau (IPX4)</li><li>Boîtier de charge MagSafe (USB-C) avec haut-parleur et attache pour dragonne</li></ul><h2>Spécifications :</h2><ul><li>Technologie audio : Haut-parleur Apple à haute excursion exclusif, Amplificateur à gamme dynamique élevée exclusif</li><li>Puce : Puce H2 pour écouteurs, Puce U1 dans le Boîtier de charge MagSafe</li><li>Autonomie : Jusqu''à 6 heures (AirPods), Jusqu''à 30 heures (avec boîtier)</li></ul>',
        'Apple AirPods Pro (2ᵉ génération)', 'Apple AirPods Pro (2ᵉ génération) USB-C',
        'Profitez d''un son supérieur avec les AirPods Pro (2ᵉ gén) dotés d''une Réduction active du bruit améliorée, de la Transparence adaptative et de l''Audio spatial personnalisé.',
        'airpods pro, apple, écouteurs, écouteurs sans fil, réduction de bruit, audio spatial, usb-c',
        'Apple AirPods Pro (2ᵉ Gén) | Son Reconstruit',
        '2x Plus de Réduction Active du Bruit', 'apple-airpods-pro-2e-gen', 'fr', 144)
on conflict (description_id) do nothing;

-- Product 145: Samsung Galaxy Buds2 Pro (Man: 20, Type: 15)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (145, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        null, false, null, true, false, 0.012, null, 'ELEC-REF-145', 9, 20,
        '65f023632bc46470c104b75f', 15) -- Type 15 (HEADPHONES)
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (289, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Samsung Galaxy Buds2 Pro</h1><p>Immerse yourself in your favorite audio with Galaxy Buds2 Pro. They deliver crystal-clear sound, Active Noise Cancellation, and a comfortable, secure fit.</p><h2>Key Features:</h2><ul><li>Intelligent Active Noise Cancellation (ANC)</li><li>Hi-Fi sound quality (24-bit Audio)</li><li>360 Audio with Direct Multi-channel</li><li>Comfortable and secure ergonomic design</li><li>IPX7 water resistance</li><li>Seamless connectivity with Galaxy devices</li></ul><h2>Specifications:</h2><ul><li>Speaker: Custom Coaxial 2-way</li><li>ANC: Yes, Intelligent ANC</li><li>Battery Life: Up to 5 hours (ANC on), Up to 18 hours (with case, ANC on)</li><li>Water Resistance: IPX7</li></ul>',
        'Samsung Galaxy Buds2 Pro', 'Samsung Galaxy Buds2 Pro - True Wireless Earbuds',
        'Galaxy Buds2 Pro offer immersive Hi-Fi sound, intelligent ANC, and a comfortable fit for the ultimate wireless audio experience.',
        'galaxy buds2 pro, samsung, earbuds, wireless earbuds, true wireless, anc, noise cancelling',
        'Samsung Galaxy Buds2 Pro | Ultimate Hi-Fi Sound',
        'Intelligent ANC, 24-bit Hi-Fi Audio', 'samsung-galaxy-buds2-pro', 'en', 145)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (290, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Samsung Galaxy Buds2 Pro</h1><p>Plongez dans votre audio préféré avec les Galaxy Buds2 Pro. Ils offrent un son cristallin, une Réduction active du bruit et un ajustement confortable et sécurisé.</p><h2>Caractéristiques Clés :</h2><ul><li>Réduction active du bruit (ANC) intelligente</li><li>Qualité sonore Hi-Fi (Audio 24 bits)</li><li>Audio 360 avec Direct Multi-channel</li><li>Design ergonomique confortable et sécurisé</li><li>Résistance à l''eau IPX7</li><li>Connectivité transparente avec les appareils Galaxy</li></ul><h2>Spécifications :</h2><ul><li>Haut-parleur : Coaxial 2 voies personnalisé</li><li>ANC : Oui, ANC intelligent</li><li>Autonomie : Jusqu''à 5 heures (ANC activé), Jusqu''à 18 heures (avec étui, ANC activé)</li><li>Résistance à l''eau : IPX7</li></ul>',
        'Samsung Galaxy Buds2 Pro', 'Samsung Galaxy Buds2 Pro - Écouteurs True Wireless',
        'Les Galaxy Buds2 Pro offrent un son Hi-Fi immersif, une ANC intelligente et un ajustement confortable pour une expérience audio sans fil ultime.',
        'galaxy buds2 pro, samsung, écouteurs, écouteurs sans fil, true wireless, anc, réduction de bruit',
        'Samsung Galaxy Buds2 Pro | Son Hi-Fi Ultime',
        'ANC Intelligent, Audio Hi-Fi 24 bits', 'samsung-galaxy-buds2-pro', 'fr', 145)
on conflict (description_id) do nothing;

-- Product 146: Sony Bravia X90L 65-inch TV (Man: 21, Type: 16)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (146, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        32.9, false, 2.2, true, false, 51.7, 57.1, 'ELEC-REF-146', 10, 21,
        '65f023632bc46470c104b75f', 16) -- Type 16 (TELEVISIONS)
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (291, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Sony BRAVIA XR X90L 65-inch Full Array LED 4K HDR Google TV</h1><p>See reality revealed with the deep contrast and vibrant colors of the Sony X90L Full Array LED TV, powered by the intelligent Cognitive Processor XR™.</p><h2>Key Features:</h2><ul><li>Full Array LED contrast and color</li><li>Cognitive Processor XR™</li><li>XR Contrast Booster & XR Triluminos Pro</li><li>Google TV with Google Assistant</li><li>Perfect for PlayStation® 5 (4K/120fps, VRR, ALLM)</li><li>Acoustic Multi-Audio™</li></ul><h2>Specifications:</h2><ul><li>Screen Size: 65 inches</li><li>Resolution: 4K Ultra HD (3840 x 2160)</li><li>Display Type: Full Array LED</li><li>Processor: Cognitive Processor XR™</li><li>Smart TV Platform: Google TV</li></ul>',
        'Sony Bravia X90L 65-inch TV', 'Sony BRAVIA XR X90L 65" Full Array LED 4K Google TV',
        'The Sony Bravia X90L 65-inch TV delivers stunning 4K HDR picture quality with Full Array LED backlighting and the intelligent Cognitive Processor XR.',
        'sony bravia x90l, full array led, 65 inch tv, 4k tv, google tv, cognitive processor xr, hdr',
        'Sony Bravia X90L 65" 4K TV | Intelligent Processing',
        'Full Array LED, Cognitive Processor XR', 'sony-bravia-x90l-65-inch-tv', 'en', 146)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (292, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Téléviseur Google TV Sony BRAVIA XR X90L 65 pouces Full Array LED 4K HDR</h1><p>Voyez la réalité révélée avec le contraste profond et les couleurs vibrantes du téléviseur Sony X90L Full Array LED, alimenté par l''intelligent Cognitive Processor XR™.</p><h2>Caractéristiques Clés :</h2><ul><li>Contraste et couleur Full Array LED</li><li>Cognitive Processor XR™</li><li>XR Contrast Booster & XR Triluminos Pro</li><li>Google TV avec Google Assistant</li><li>Parfait pour PlayStation® 5 (4K/120fps, VRR, ALLM)</li><li>Acoustic Multi-Audio™</li></ul><h2>Spécifications :</h2><ul><li>Taille de l''écran : 65 pouces</li><li>Résolution : 4K Ultra HD (3840 x 2160)</li><li>Type d''affichage : Full Array LED</li><li>Processeur : Cognitive Processor XR™</li><li>Plateforme Smart TV : Google TV</li></ul>',
        'Téléviseur Sony Bravia X90L 65 pouces', 'Téléviseur Google TV Sony BRAVIA XR X90L 65" Full Array LED 4K',
        'Le téléviseur Sony Bravia X90L 65 pouces offre une qualité d''image 4K HDR époustouflante avec rétroéclairage Full Array LED et l''intelligent Cognitive Processor XR.',
        'sony bravia x90l, full array led, tv 65 pouces, tv 4k, google tv, cognitive processor xr, hdr',
        'Téléviseur Sony Bravia X90L 65" 4K | Traitement Intelligent',
        'Full Array LED, Cognitive Processor XR', 'sony-bravia-x90l-65-pouces-tv', 'fr', 146)
on conflict (description_id) do nothing;

-- Product 147: Samsung Galaxy A54 (Man: 20, Type: 13)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (147, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        6.23, false, 3.07, true, false, 0.45, 0.32, 'ELEC-REF-147', 11, 20,
        '65f023632bc46470c104b75f', 13) -- Type 13 (SMARTPHONES)
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (293, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Samsung Galaxy A54 5G</h1><p>Awesome is for everyone. Galaxy A54 5G boasts a premium design, advanced cameras, and a smooth 120Hz display, making flagship features accessible.</p><h2>Key Features:</h2><ul><li>6.4-inch Super AMOLED 120Hz display</li><li>50MP Main Camera with OIS</li><li>Premium glass finish</li><li>IP67 water and dust resistance</li><li>Long-lasting 5,000mAh battery</li><li>5G connectivity</li></ul><h2>Specifications:</h2><ul><li>Display: 6.4" FHD+ Super AMOLED, 120Hz</li><li>Processor: Exynos 1380</li><li>RAM: 6GB / 8GB</li><li>Storage: 128GB / 256GB (expandable via microSD)</li><li>Battery: 5,000mAh</li></ul>',
        'Samsung Galaxy A54 5G', 'Samsung Galaxy A54 5G - Awesome Camera & Display',
        'The Samsung Galaxy A54 5G offers a premium experience with a 120Hz Super AMOLED display, 50MP OIS camera, and long battery life.',
        'galaxy a54, samsung, smartphone, android, mid-range phone, 5g, super amoled',
        'Samsung Galaxy A54 5G | Awesome for Everyone',
        '120Hz Display, 50MP OIS Camera', 'samsung-galaxy-a54-5g', 'en', 147)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (294, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Samsung Galaxy A54 5G</h1><p>L''incroyable est pour tout le monde. Le Galaxy A54 5G arbore un design premium, des appareils photo avancés et un écran fluide 120Hz, rendant les fonctionnalités phares accessibles.</p><h2>Caractéristiques Clés :</h2><ul><li>Écran Super AMOLED 120Hz 6,4 pouces</li><li>Appareil photo principal 50 Mpx avec OIS</li><li>Finition en verre premium</li><li>Résistance à l''eau et à la poussière IP67</li><li>Batterie longue durée 5 000 mAh</li><li>Connectivité 5G</li></ul><h2>Spécifications :</h2><ul><li>Écran : 6,4" FHD+ Super AMOLED, 120Hz</li><li>Processeur : Exynos 1380</li><li>RAM : 6 Go / 8 Go</li><li>Stockage : 128 Go / 256 Go (extensible via microSD)</li><li>Batterie : 5 000 mAh</li></ul>',
        'Samsung Galaxy A54 5G', 'Samsung Galaxy A54 5G - Super Appareil Photo et Écran',
        'Le Samsung Galaxy A54 5G offre une expérience premium avec un écran Super AMOLED 120Hz, un appareil photo 50MP OIS et une longue autonomie.',
        'galaxy a54, samsung, smartphone, android, téléphone milieu de gamme, 5g, super amoled',
        'Samsung Galaxy A54 5G | Incroyable pour Tous',
        'Écran 120Hz, Caméra 50MP OIS', 'samsung-galaxy-a54-5g', 'fr', 147)
on conflict (description_id) do nothing;

-- Product 148: Sony LinkBuds S (Man: 21, Type: 15)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (148, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        null, false, null, true, false, 0.01, null, 'ELEC-REF-148', 12, 21,
        '65f023632bc46470c104b75f', 15) -- Type 15 (HEADPHONES)
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (295, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Sony LinkBuds S True Wireless Noise Canceling Earbuds</h1><p>Small, light and smart with a secure and stable fit for hours of comfort. LinkBuds S automatically switch between superlative noise canceling and optimized ambient sound for listening without distractions.</p><h2>Key Features:</h2><ul><li>Smallest and lightest Hi-Res, noise canceling earbuds</li><li>Automatic switching between ambient sound and noise canceling</li><li>Immersive sound quality with Integrated Processor V1</li><li>Crystal-clear call quality</li><li>IPX4 water resistance</li><li>Up to 20 hours battery life with charging case</li></ul><h2>Specifications:</h2><ul><li>Weight: Approx. 4.8 g x 2</li><li>Driver Unit: 5 mm</li><li>Waterproof: Yes (IPX4)</li><li>Noise Canceling: Yes</li><li>Battery Life: Max. 6 hours (NC On), Max. 9 hours (NC Off)</li></ul>',
        'Sony LinkBuds S Earbuds', 'Sony LinkBuds S True Wireless Noise Canceling Earbuds',
        'Sony LinkBuds S are ultra-small, light Hi-Res noise canceling earbuds that automatically adapt ambient sound settings.',
        'sony linkbuds s, earbuds, true wireless, noise cancelling, hi-res audio, sony earbuds',
        'Sony LinkBuds S | Small, Light, Smart',
        'Smallest & Lightest Hi-Res NC', 'sony-linkbuds-s-earbuds', 'en', 148)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (296, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Écouteurs True Wireless à Réduction de Bruit Sony LinkBuds S</h1><p>Petits, légers et intelligents avec un ajustement sûr et stable pour des heures de confort. Les LinkBuds S basculent automatiquement entre une réduction de bruit superlative et un son ambiant optimisé pour une écoute sans distractions.</p><h2>Caractéristiques Clés :</h2><ul><li>Écouteurs Hi-Res à réduction de bruit les plus petits et légers</li><li>Basculement automatique entre son ambiant et réduction de bruit</li><li>Qualité sonore immersive avec le processeur intégré V1</li><li>Qualité d''appel cristalline</li><li>Résistance à l''eau IPX4</li><li>Jusqu''à 20 heures d''autonomie avec l''étui de charge</li></ul><h2>Spécifications :</h2><ul><li>Poids : Env. 4,8 g x 2</li><li>Diaphragme : 5 mm</li><li>Étanche : Oui (IPX4)</li><li>Réduction de bruit : Oui</li><li>Autonomie : Max. 6 heures (NC On), Max. 9 heures (NC Off)</li></ul>',
        'Écouteurs Sony LinkBuds S', 'Écouteurs True Wireless à Réduction de Bruit Sony LinkBuds S',
        'Les Sony LinkBuds S sont des écouteurs Hi-Res ultra-petits et légers à réduction de bruit qui adaptent automatiquement les paramètres de son ambiant.',
        'sony linkbuds s, écouteurs, true wireless, réduction de bruit, audio hi-res, écouteurs sony',
        'Sony LinkBuds S | Petits, Légers, Intelligents',
        'Plus Petits & Légers Hi-Res NC', 'sony-linkbuds-s-ecouteurs', 'fr', 148)
on conflict (description_id) do nothing;

-- Product 149: Dell Inspiron 16 Laptop (Man: 23, Type: 14)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (149, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        0.72, false, 9.92, true, false, 4.12, 14.05, 'ELEC-REF-149', 13, 23,
        '65f023632bc46470c104b75f', 14) -- Type 14 (LAPTOPS)
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (297, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Dell Inspiron 16 Laptop</h1><p>See more on a spacious 16-inch display with a 16:10 aspect ratio. Powered by latest Intel® Core™ processors, the Inspiron 16 offers responsive performance for everyday tasks and entertainment.</p><h2>Key Features:</h2><ul><li>16-inch FHD+ display (16:10 aspect ratio)</li><li>Intel® Core™ processors (13th Gen or newer)</li><li>ComfortView software to reduce blue light</li><li>ExpressCharge capability</li><li>Up-firing speakers</li><li>Windows 11</li></ul><h2>Specifications:</h2><ul><li>Display: 16.0-inch FHD+ (1920 x 1200)</li><li>Processor: Intel® Core™ i5 / i7</li><li>RAM: 8GB / 16GB DDR4</li><li>Storage: 512GB / 1TB NVMe SSD</li></ul>',
        'Dell Inspiron 16 Laptop', 'Dell Inspiron 16 - Large Display Laptop',
        'The Dell Inspiron 16 laptop features a large 16-inch 16:10 display and reliable performance for productivity and entertainment.',
        'dell inspiron 16, laptop, windows 11, intel core, 16 inch laptop',
        'Dell Inspiron 16 Laptop | See More, Do More',
        '16-inch 16:10 Display', 'dell-inspiron-16-laptop', 'en', 149)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (298, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Ordinateur Portable Dell Inspiron 16</h1><p>Voyez plus grand sur un écran spacieux de 16 pouces avec un format 16:10. Alimenté par les derniers processeurs Intel® Core™, l''Inspiron 16 offre des performances réactives pour les tâches quotidiennes et le divertissement.</p><h2>Caractéristiques Clés :</h2><ul><li>Écran FHD+ 16 pouces (format 16:10)</li><li>Processeurs Intel® Core™ (13e génération ou plus récent)</li><li>Logiciel ComfortView pour réduire la lumière bleue</li><li>Capacité ExpressCharge</li><li>Haut-parleurs orientés vers le haut</li><li>Windows 11</li></ul><h2>Spécifications :</h2><ul><li>Écran : 16,0 pouces FHD+ (1920 x 1200)</li><li>Processeur : Intel® Core™ i5 / i7</li><li>RAM : 8 Go / 16 Go DDR4</li><li>Stockage : 512 Go / 1 To SSD NVMe</li></ul>',
        'Ordinateur Portable Dell Inspiron 16', 'Dell Inspiron 16 - Ordinateur Portable Grand Écran',
        'L''ordinateur portable Dell Inspiron 16 est doté d''un grand écran 16 pouces 16:10 et de performances fiables pour la productivité et le divertissement.',
        'dell inspiron 16, ordinateur portable, windows 11, intel core, portable 16 pouces',
        'Ordinateur Portable Dell Inspiron 16 | Voyez Plus, Faites Plus',
        'Écran 16 pouces 16:10', 'dell-inspiron-16-portable', 'fr', 149)
on conflict (description_id) do nothing;

-- Product 150: LG Gram 17 Laptop (Man: 22, Type: 14)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (150, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        0.70, false, 10.18, true, false, 2.98, 14.91, 'ELEC-REF-150', 14, 22,
        '65f023632bc46470c104b75f', 14) -- Type 14 (LAPTOPS)
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (299, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>LG gram 17" Lightweight Laptop</h1><p>Experience ultimate portability without sacrificing screen size. The LG gram 17 boasts a large 17-inch display in an incredibly lightweight chassis, powered by Intel® Evo™ platform.</p><h2>Key Features:</h2><ul><li>17-inch WQXGA IPS display (16:10 aspect ratio)</li><li>Intel® Evo™ platform with Intel® Core™ processors</li><li>Ultra-lightweight design (under 3 lbs)</li><li>Long battery life</li><li>DCI-P3 99% color gamut</li><li>Thunderbolt™ 4 connectivity</li><li>Windows 11</li></ul><h2>Specifications:</h2><ul><li>Display: 17.0" WQXGA (2560 x 1600) IPS</li><li>Processor: Intel® Core™ i5 / i7 (Intel® Evo™ Edition)</li><li>RAM: 16GB LPDDR5</li><li>Storage: 512GB / 1TB / 2TB NVMe SSD</li></ul>',
        'LG gram 17 Laptop', 'LG gram 17" Ultra-Lightweight Laptop with Intel Evo',
        'The LG gram 17 offers a large 17-inch screen in an unbelievably lightweight design, featuring Intel Evo performance and long battery life.',
        'lg gram 17, laptop, lightweight laptop, intel evo, 17 inch laptop, windows 11',
        'LG gram 17 Laptop | Large Screen, Light Weight',
        'Ultra-Lightweight, 17-inch Display', 'lg-gram-17-laptop', 'en', 150)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (300, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Ordinateur Portable Léger LG gram 17"</h1><p>Profitez d''une portabilité ultime sans sacrifier la taille de l''écran. Le LG gram 17 arbore un grand écran de 17 pouces dans un châssis incroyablement léger, alimenté par la plateforme Intel® Evo™.</p><h2>Caractéristiques Clés :</h2><ul><li>Écran IPS WQXGA 17 pouces (format 16:10)</li><li>Plateforme Intel® Evo™ avec processeurs Intel® Core™</li><li>Design ultra-léger (moins de 1,35 kg)</li><li>Longue autonomie</li><li>Gamme de couleurs DCI-P3 99%</li><li>Connectivité Thunderbolt™ 4</li><li>Windows 11</li></ul><h2>Spécifications :</h2><ul><li>Écran : 17,0" WQXGA (2560 x 1600) IPS</li><li>Processeur : Intel® Core™ i5 / i7 (Édition Intel® Evo™)</li><li>RAM : 16 Go LPDDR5</li><li>Stockage : 512 Go / 1 To / 2 To SSD NVMe</li></ul>',
        'Ordinateur Portable LG gram 17', 'Ordinateur Portable Ultra-Léger LG gram 17" avec Intel Evo',
        'Le LG gram 17 offre un grand écran de 17 pouces dans un design incroyablement léger, avec les performances Intel Evo et une longue autonomie.',
        'lg gram 17, ordinateur portable, portable léger, intel evo, portable 17 pouces, windows 11',
        'Ordinateur Portable LG gram 17 | Grand Écran, Poids Plume',
        'Ultra-Léger, Écran 17 pouces', 'lg-gram-17-portable', 'fr', 150)
on conflict (description_id) do nothing;
-- Product 151: HP Envy x360 16 Laptop (Man: 24, Type: 14)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (151, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        0.77, false, 9.02, true, false, 4.13, 14.11, 'ELEC-REF-151', 15, 24,
        '65f023632bc46470c104b75f', 14) -- Type 14 (LAPTOPS)
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (301, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>HP Envy x360 16 2-in-1 Laptop</h1><p>Unleash your creativity with the versatile HP Envy x360 16. Featuring a large 16-inch display, powerful performance, and a 360-degree hinge for flexibility.</p><h2>Key Features:</h2><ul><li>16-inch Touchscreen Display (FHD or OLED options)</li><li>Intel® Core™ or AMD Ryzen™ processors</li><li>360° hinge for laptop, tablet, tent, and stand modes</li><li>Sustainable design with recycled materials</li><li>HP Presence 5MP IR camera</li><li>Long battery life</li><li>Windows 11</li></ul><h2>Specifications:</h2><ul><li>Display: 16" FHD or OLED Touchscreen</li><li>Processor: Intel® Core™ i5/i7 or AMD Ryzen™ 5/7</li><li>RAM: 8GB / 16GB DDR4</li><li>Storage: 512GB / 1TB PCIe® NVMe™ SSD</li></ul>',
        'HP Envy x360 16 Laptop', 'HP Envy x360 16 2-in-1 Laptop - Versatile & Powerful',
        'The HP Envy x360 16 is a versatile 2-in-1 laptop with a large touchscreen, powerful processor options, and a sustainable design.',
        'hp envy x360 16, 2-in-1 laptop, convertible laptop, windows 11, intel core, amd ryzen, touchscreen',
        'HP Envy x360 16 | Create & Connect',
        '16-inch Touchscreen, 360° Hinge', 'hp-envy-x360-16-laptop', 'en', 151)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (302, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Ordinateur Portable 2-en-1 HP Envy x360 16</h1><p>Libérez votre créativité avec le polyvalent HP Envy x360 16. Doté d''un grand écran de 16 pouces, de performances puissantes et d''une charnière à 360 degrés pour plus de flexibilité.</p><h2>Caractéristiques Clés :</h2><ul><li>Écran Tactile 16 pouces (options FHD ou OLED)</li><li>Processeurs Intel® Core™ ou AMD Ryzen™</li><li>Charnière à 360° pour modes portable, tablette, tente et chevalet</li><li>Conception durable avec matériaux recyclés</li><li>Caméra IR HP Presence 5MP</li><li>Longue autonomie</li><li>Windows 11</li></ul><h2>Spécifications :</h2><ul><li>Écran : Tactile 16" FHD ou OLED</li><li>Processeur : Intel® Core™ i5/i7 ou AMD Ryzen™ 5/7</li><li>RAM : 8 Go / 16 Go DDR4</li><li>Stockage : 512 Go / 1 To SSD PCIe® NVMe™</li></ul>',
        'Ordinateur Portable HP Envy x360 16', 'Ordinateur Portable 2-en-1 HP Envy x360 16 - Polyvalent et Puissant',
        'Le HP Envy x360 16 est un ordinateur portable 2-en-1 polyvalent avec un grand écran tactile, des options de processeur puissantes et une conception durable.',
        'hp envy x360 16, portable 2-en-1, portable convertible, windows 11, intel core, amd ryzen, écran tactile',
        'HP Envy x360 16 | Créez et Connectez',
        'Écran Tactile 16 pouces, Charnière 360°', 'hp-envy-x360-16-portable', 'fr', 151)
on conflict (description_id) do nothing;

-- Product 152: Apple TV 4K (2022) (Man: 19, Type: 16)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (152, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        1.2, false, 3.66, true, false, 0.47, 3.66, 'ELEC-REF-152', 16, 19,
        '65f023632bc46470c104b75f', 16) -- Type 16 (TELEVISIONS - *Streaming Device*)
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (303, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Apple TV 4K (2022)</h1><p>The future of television is here. Apple TV 4K brings the best shows, movies, sports, and live TV together with your favorite Apple devices and services. Now with 4K High Frame Rate HDR for fluid, crisp video.</p><h2>Key Features:</h2><ul><li>A15 Bionic chip</li><li>4K High Frame Rate HDR with Dolby Vision</li><li>Dolby Atmos for immersive sound</li><li>Siri Remote with touch-enabled clickpad</li><li>Access to Apple TV+, Apple Music, Apple Arcade, and the App Store</li><li>Acts as a home hub for HomeKit accessories</li></ul><h2>Specifications:</h2><ul><li>Processor: A15 Bionic chip</li><li>Storage: 64GB or 128GB</li><li>Video Output: Up to 4K HDR, Dolby Vision</li><li>Audio Output: Dolby Atmos</li><li>Connectivity: Wi-Fi 6, Ethernet (128GB model), Bluetooth 5.0</li></ul>',
        'Apple TV 4K (2022)', 'Apple TV 4K (3rd generation) - Wi-Fi + Ethernet',
        'Experience stunning 4K HDR streaming, immersive Dolby Atmos sound, and powerful performance with the Apple TV 4K powered by the A15 Bionic chip.',
        'apple tv 4k, streaming device, 4k hdr, dolby vision, dolby atmos, a15 bionic, siri remote',
        'Apple TV 4K (2022) | Cinematic Experience',
        'A15 Bionic Chip, 4K HDR', 'apple-tv-4k-2022', 'en', 152)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (304, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Apple TV 4K (2022)</h1><p>Le futur de la télévision est là. L’Apple TV 4K réunit les meilleures séries, films, événements sportifs et chaînes en direct, ainsi que vos appareils et services Apple préférés. Désormais avec la technologie HDR 4K à fréquence d’images élevée pour une vidéo fluide et nette.</p><h2>Caractéristiques Clés :</h2><ul><li>Puce A15 Bionic</li><li>HDR 4K à fréquence d’images élevée avec Dolby Vision</li><li>Dolby Atmos pour un son immersif</li><li>Siri Remote avec clickpad tactile</li><li>Accès à Apple TV+, Apple Music, Apple Arcade et à l’App Store</li><li>Sert de hub domestique pour les accessoires HomeKit</li></ul><h2>Spécifications :</h2><ul><li>Processeur : Puce A15 Bionic</li><li>Stockage : 64 Go ou 128 Go</li><li>Sortie vidéo : Jusqu’à 4K HDR, Dolby Vision</li><li>Sortie audio : Dolby Atmos</li><li>Connectivité : Wi‑Fi 6, Ethernet (modèle 128 Go), Bluetooth 5.0</li></ul>',
        'Apple TV 4K (2022)', 'Apple TV 4K (3ᵉ génération) - Wi‑Fi + Ethernet',
        'Profitez d''un streaming 4K HDR époustouflant, d''un son immersif Dolby Atmos et de performances puissantes avec l''Apple TV 4K, grâce à la puce A15 Bionic.',
        'apple tv 4k, lecteur streaming, 4k hdr, dolby vision, dolby atmos, a15 bionic, siri remote',
        'Apple TV 4K (2022) | Expérience Cinématographique',
        'Puce A15 Bionic, 4K HDR', 'apple-tv-4k-2022', 'fr', 152)
on conflict (description_id) do nothing;

-- Product 153: Samsung The Frame TV (55-inch) (Man: 20, Type: 16)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (153, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        27.9, false, 1.0, true, false, 35.3, 48.6, 'ELEC-REF-153', 17, 20,
        '65f023632bc46470c104b75f', 16) -- Type 16 (TELEVISIONS)
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (305, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Samsung 55" The Frame QLED 4K Smart TV</h1><p>TV when it''s on, art when it''s off. The Frame transforms your living space with a stunning QLED 4K display and access to a world of art when not in use.</p><h2>Key Features:</h2><ul><li>Art Mode with Art Store</li><li>Matte Display for reduced glare</li><li>Customizable Bezels (sold separately)</li><li>QLED 4K picture quality with 100% Color Volume</li><li>Quantum Processor 4K</li><li>Tizen Smart TV Hub</li></ul><h2>Specifications:</h2><ul><li>Screen Size: 55 inches</li><li>Resolution: 4K Ultra HD (3840 x 2160)</li><li>Display Type: QLED with Matte Display</li><li>Processor: Quantum Processor 4K</li><li>Smart TV Platform: Tizen</li></ul>',
        'Samsung 55" The Frame TV', 'Samsung 55" The Frame QLED 4K Smart TV - Art Mode',
        'Transform your space with Samsung The Frame TV. Enjoy QLED 4K picture quality and display beautiful artwork with Art Mode on a Matte Display.',
        'samsung the frame, qled tv, 55 inch tv, 4k tv, smart tv, art mode, matte display',
        'Samsung The Frame 55" TV | TV & Art Combined',
        'Art Mode, Matte Display', 'samsung-the-frame-55-inch-tv', 'en', 153)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (306, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Téléviseur Intelligent Samsung 55" The Frame QLED 4K</h1><p>La télé quand elle est allumée, de l''art quand elle est éteinte. The Frame transforme votre espace de vie avec un superbe écran QLED 4K et l''accès à un monde d''art lorsqu''il n''est pas utilisé.</p><h2>Caractéristiques Clés :</h2><ul><li>Mode Art avec Art Store</li><li>Écran Mat pour réduire les reflets</li><li>Cadres personnalisables (vendus séparément)</li><li>Qualité d''image QLED 4K avec 100% Volume Couleur</li><li>Quantum Processor 4K</li><li>Hub Smart TV Tizen</li></ul><h2>Spécifications :</h2><ul><li>Taille de l''écran : 55 pouces</li><li>Résolution : 4K Ultra HD (3840 x 2160)</li><li>Type d''affichage : QLED avec Écran Mat</li><li>Processeur : Quantum Processor 4K</li><li>Plateforme Smart TV : Tizen</li></ul>',
        'Téléviseur Samsung 55" The Frame', 'Téléviseur Intelligent Samsung 55" The Frame QLED 4K - Mode Art',
        'Transformez votre espace avec le téléviseur Samsung The Frame. Profitez de la qualité d''image QLED 4K et affichez de magnifiques œuvres d''art avec le Mode Art sur un Écran Mat.',
        'samsung the frame, tv qled, tv 55 pouces, tv 4k, smart tv, mode art, écran mat',
        'Téléviseur Samsung The Frame 55" | TV et Art Combinés',
        'Mode Art, Écran Mat', 'samsung-the-frame-55-pouces-tv', 'fr', 153)
on conflict (description_id) do nothing;

-- Product 154: Sony INZONE H9 Wireless Headset (Man: 21, Type: 15)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (154, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        null, false, null, true, false, 0.71, null, 'ELEC-REF-154', 18, 21,
        '65f023632bc46470c104b75f', 15) -- Type 15 (HEADPHONES)
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (307, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Sony INZONE H9 Wireless Noise Canceling Gaming Headset</h1><p>Sharpen your senses and prepare for victory. The INZONE H9 headset immerses you in the zone with spatial sound and noise canceling for supercharged hearing and sharp reflexes.</p><h2>Key Features:</h2><ul><li>Precise target detection with 360 Spatial Sound for Gaming</li><li>Dual Sensor Noise Canceling Technology</li><li>Soft headband cushion and synthetic leather ear pads for comfort</li><li>Flip-up boom microphone with mute function</li><li>Up to 32 hours of battery life</li><li>Perfect for PlayStation® 5</li></ul><h2>Specifications:</h2><ul><li>Headphone Type: Closed, Dynamic</li><li>Driver Unit: 40mm, Dome type</li><li>Noise Canceling: Yes</li><li>Battery Life: Max. 32 hours (NC OFF)</li><li>Connectivity: Bluetooth 5.0, 2.4GHz Wireless</li></ul>',
        'Sony INZONE H9 Gaming Headset', 'Sony INZONE H9 Wireless Noise Canceling Gaming Headset',
        'Immerse yourself in the game with the Sony INZONE H9 headset, featuring 360 Spatial Sound, noise canceling, and long-lasting comfort.',
        'sony inzone h9, gaming headset, wireless headset, noise cancelling, spatial sound, ps5 headset',
        'Sony INZONE H9 | Immerse in Victory',
        '360 Spatial Sound, Noise Canceling', 'sony-inzone-h9-gaming-headset', 'en', 154)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (308, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Casque Gaming sans Fil à Réduction de Bruit Sony INZONE H9</h1><p>Aiguisez vos sens et préparez-vous à la victoire. Le casque INZONE H9 vous plonge dans l''action avec le son spatial et la réduction de bruit pour une audition suralimentée et des réflexes aiguisés.</p><h2>Caractéristiques Clés :</h2><ul><li>Détection précise des cibles avec le Son Spatial 360 pour le jeu</li><li>Technologie de réduction de bruit à double capteur</li><li>Coussinet d''arceau souple et coussinets d''oreille en cuir synthétique pour le confort</li><li>Microphone sur tige relevable avec fonction mute</li><li>Jusqu''à 32 heures d''autonomie</li><li>Parfait pour PlayStation® 5</li></ul><h2>Spécifications :</h2><ul><li>Type de casque : Fermé, Dynamique</li><li>Diaphragme : 40 mm, type dôme</li><li>Réduction de bruit : Oui</li><li>Autonomie : Max. 32 heures (NC OFF)</li><li>Connectivité : Bluetooth 5.0, Sans fil 2,4 GHz</li></ul>',
        'Casque Gaming Sony INZONE H9', 'Casque Gaming sans Fil à Réduction de Bruit Sony INZONE H9',
        'Plongez au cœur du jeu avec le casque Sony INZONE H9, doté du Son Spatial 360, de la réduction de bruit et d''un confort durable.',
        'sony inzone h9, casque gaming, casque sans fil, réduction de bruit, son spatial, casque ps5',
        'Sony INZONE H9 | Plongez dans la Victoire',
        'Son Spatial 360, Réduction de Bruit', 'sony-inzone-h9-casque-gaming', 'fr', 154)
on conflict (description_id) do nothing;

-- Product 155: LG UltraGear 27" OLED Monitor (Man: 22, Type: 16)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (155, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        15.6, false, 10.3, true, false, 11.2, 23.8, 'ELEC-REF-155', 19, 22,
        '65f023632bc46470c104b75f', 16) -- Type 16 (TELEVISIONS - *Monitor*)
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (309, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>LG UltraGear™ 27" OLED QHD Gaming Monitor</h1><p>Experience breathtaking gaming with the LG UltraGear OLED monitor. Featuring a 240Hz refresh rate, 0.03ms response time, and stunning OLED picture quality.</p><h2>Key Features:</h2><ul><li>27-inch QHD (2560 x 1440) OLED Display</li><li>240Hz Refresh Rate & 0.03ms (GtG) Response Time</li><li>NVIDIA® G-SYNC® Compatible & AMD FreeSync™ Premium</li><li>OLED Color Calibrated</li><li>Anti-Glare Low Reflection screen</li><li>4-Side Virtually Borderless Design</li></ul><h2>Specifications:</h2><ul><li>Screen Size: 27 inches</li><li>Resolution: QHD (2560 x 1440)</li><li>Panel Type: OLED</li><li>Refresh Rate: 240Hz</li><li>Response Time: 0.03ms (GtG)</li></ul>',
        'LG UltraGear 27" OLED Monitor', 'LG UltraGear 27" OLED QHD 240Hz Gaming Monitor',
        'Elevate your gaming with the LG UltraGear 27-inch OLED monitor, offering a blazing-fast 240Hz refresh rate and incredible contrast.',
        'lg ultragear, gaming monitor, oled monitor, 27 inch monitor, qhd, 240hz, g-sync, freesync',
        'LG UltraGear 27" OLED Monitor | Blazing Speed',
        '240Hz OLED Display, 0.03ms Response', 'lg-ultragear-27-oled-monitor', 'en', 155)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (310, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Moniteur Gaming LG UltraGear™ 27" OLED QHD</h1><p>Vivez une expérience de jeu époustouflante avec le moniteur LG UltraGear OLED. Doté d''un taux de rafraîchissement de 240 Hz, d''un temps de réponse de 0,03 ms et d''une qualité d''image OLED stupéfiante.</p><h2>Caractéristiques Clés :</h2><ul><li>Écran OLED QHD 27 pouces (2560 x 1440)</li><li>Taux de rafraîchissement 240 Hz & Temps de réponse 0,03 ms (GtG)</li><li>Compatible NVIDIA® G-SYNC® & AMD FreeSync™ Premium</li><li>Couleurs OLED calibrées</li><li>Écran Anti-reflet à faible réflexion</li><li>Design pratiquement sans bordure sur 4 côtés</li></ul><h2>Spécifications :</h2><ul><li>Taille de l''écran : 27 pouces</li><li>Résolution : QHD (2560 x 1440)</li><li>Type de dalle : OLED</li><li>Taux de rafraîchissement : 240 Hz</li><li>Temps de réponse : 0,03 ms (GtG)</li></ul>',
        'Moniteur LG UltraGear 27" OLED', 'Moniteur Gaming LG UltraGear 27" OLED QHD 240Hz',
        'Améliorez votre jeu avec le moniteur LG UltraGear 27 pouces OLED, offrant un taux de rafraîchissement ultra-rapide de 240 Hz et un contraste incroyable.',
        'lg ultragear, moniteur gaming, moniteur oled, moniteur 27 pouces, qhd, 240hz, g-sync, freesync',
        'Moniteur LG UltraGear 27" OLED | Vitesse Fulgurante',
        'Écran OLED 240Hz, Réponse 0,03ms', 'lg-ultragear-27-oled-moniteur', 'fr', 155)
on conflict (description_id) do nothing;

-- Product 156: Dell Alienware m16 Gaming Laptop (Man: 23, Type: 14)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (156, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        1.0, false, 11.41, true, false, 6.65, 14.5, 'ELEC-REF-156', 20, 23,
        '65f023632bc46470c104b75f', 14) -- Type 14 (LAPTOPS)
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (311, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Alienware m16 Gaming Laptop</h1><p>Dominate the competition with the Alienware m16. Featuring high-performance processors, powerful NVIDIA® GeForce RTX™ graphics, and an advanced cooling system.</p><h2>Key Features:</h2><ul><li>16-inch QHD+ 240Hz display option</li><li>Latest Intel® Core™ or AMD Ryzen™ processors</li><li>NVIDIA® GeForce RTX™ 40 Series Laptop GPUs</li><li>Alienware Cryo-tech™ cooling technology</li><li>AlienFX RGB keyboard</li><li>Windows 11</li></ul><h2>Specifications:</h2><ul><li>Display: 16" QHD+ (2560 x 1600) 165Hz or 240Hz</li><li>Processor: Intel® Core™ i7/i9 or AMD Ryzen™ 7/9</li><li>Graphics: NVIDIA® GeForce RTX™ 4060 / 4070 / 4080 / 4090</li><li>RAM: 16GB / 32GB / 64GB DDR5</li><li>Storage: Up to 4TB NVMe SSD</li></ul>',
        'Alienware m16 Gaming Laptop', 'Alienware m16 - High-Performance Gaming Laptop',
        'Unleash peak gaming performance with the Alienware m16 laptop, equipped with powerful processors, RTX 40 Series graphics, and Cryo-tech cooling.',
        'alienware m16, gaming laptop, dell, nvidia rtx 40 series, intel core, amd ryzen, 240hz',
        'Alienware m16 Gaming Laptop | Dominate',
        'RTX 40 Series, 240Hz Display Option', 'alienware-m16-gaming-laptop', 'en', 156)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (312, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Ordinateur Portable Gaming Alienware m16</h1><p>Dominez la concurrence avec l''Alienware m16. Doté de processeurs haute performance, de puissantes cartes graphiques NVIDIA® GeForce RTX™ et d''un système de refroidissement avancé.</p><h2>Caractéristiques Clés :</h2><ul><li>Option d''écran QHD+ 16 pouces 240 Hz</li><li>Derniers processeurs Intel® Core™ ou AMD Ryzen™</li><li>GPU pour portable NVIDIA® GeForce RTX™ série 40</li><li>Technologie de refroidissement Alienware Cryo-tech™</li><li>Clavier AlienFX RGB</li><li>Windows 11</li></ul><h2>Spécifications :</h2><ul><li>Écran : 16" QHD+ (2560 x 1600) 165 Hz ou 240 Hz</li><li>Processeur : Intel® Core™ i7/i9 ou AMD Ryzen™ 7/9</li><li>Graphiques : NVIDIA® GeForce RTX™ 4060 / 4070 / 4080 / 4090</li><li>RAM : 16 Go / 32 Go / 64 Go DDR5</li><li>Stockage : Jusqu''à 4 To SSD NVMe</li></ul>',
        'Ordinateur Portable Gaming Alienware m16', 'Alienware m16 - Ordinateur Portable Gaming Haute Performance',
        'Libérez des performances de jeu de pointe avec l''ordinateur portable Alienware m16, équipé de processeurs puissants, de graphiques RTX série 40 et du refroidissement Cryo-tech.',
        'alienware m16, portable gaming, dell, nvidia rtx 40 series, intel core, amd ryzen, 240hz',
        'Ordinateur Portable Gaming Alienware m16 | Dominez',
        'RTX Série 40, Option Écran 240Hz', 'alienware-m16-portable-gaming', 'fr', 156)
on conflict (description_id) do nothing;

-- Product 157: HP OMEN 16 Gaming Laptop (Man: 24, Type: 14)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (157, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        0.93, false, 10.21, true, false, 5.29, 14.53, 'ELEC-REF-157', 21, 24,
        '65f023632bc46470c104b75f', 14) -- Type 14 (LAPTOPS)
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (313, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>OMEN Gaming Laptop 16</h1><p>Play at your best with the OMEN Gaming Laptop 16. Powered by the latest processors and graphics, featuring an enhanced cooling system and a high-refresh-rate display.</p><h2>Key Features:</h2><ul><li>16.1-inch display (up to QHD 240Hz)</li><li>Intel® Core™ or AMD Ryzen™ processors</li><li>NVIDIA® GeForce RTX™ graphics</li><li>OMEN Tempest Cooling Technology</li><li>OMEN Gaming Hub for customization</li><li>Windows 11</li></ul><h2>Specifications:</h2><ul><li>Display: 16.1" FHD 144Hz or QHD 165Hz/240Hz</li><li>Processor: Intel® Core™ i7/i9 or AMD Ryzen™ 7/9</li><li>Graphics: NVIDIA® GeForce RTX™ 30/40 Series</li><li>RAM: 16GB / 32GB DDR5</li><li>Storage: 512GB / 1TB / 2TB PCIe® NVMe™ SSD</li></ul>',
        'OMEN Gaming Laptop 16', 'OMEN by HP Laptop 16 - Powerful Gaming Performance',
        'Experience top-tier gaming with the OMEN Laptop 16, featuring powerful hardware, advanced cooling, and a high-refresh-rate screen.',
        'omen 16, hp omen, gaming laptop, nvidia rtx, intel core, amd ryzen, 240hz',
        'OMEN Gaming Laptop 16 | Play Your Best',
        'Up to QHD 240Hz Display, Tempest Cooling', 'omen-gaming-laptop-16', 'en', 157)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (314, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Ordinateur Portable Gaming OMEN 16</h1><p>Jouez à votre meilleur niveau avec l''ordinateur portable gaming OMEN 16. Alimenté par les derniers processeurs et cartes graphiques, doté d''un système de refroidissement amélioré et d''un écran à taux de rafraîchissement élevé.</p><h2>Caractéristiques Clés :</h2><ul><li>Écran 16,1 pouces (jusqu''à QHD 240 Hz)</li><li>Processeurs Intel® Core™ ou AMD Ryzen™</li><li>Cartes graphiques NVIDIA® GeForce RTX™</li><li>Technologie de refroidissement OMEN Tempest</li><li>OMEN Gaming Hub pour la personnalisation</li><li>Windows 11</li></ul><h2>Spécifications :</h2><ul><li>Écran : 16,1" FHD 144 Hz ou QHD 165 Hz/240 Hz</li><li>Processeur : Intel® Core™ i7/i9 ou AMD Ryzen™ 7/9</li><li>Graphiques : NVIDIA® GeForce RTX™ séries 30/40</li><li>RAM : 16 Go / 32 Go DDR5</li><li>Stockage : 512 Go / 1 To / 2 To SSD PCIe® NVMe™</li></ul>',
        'Ordinateur Portable Gaming OMEN 16', 'Ordinateur Portable OMEN by HP 16 - Performances Gaming Puissantes',
        'Vivez une expérience de jeu de haut niveau avec l''ordinateur portable OMEN 16, doté d''un matériel puissant, d''un refroidissement avancé et d''un écran à taux de rafraîchissement élevé.',
        'omen 16, hp omen, portable gaming, nvidia rtx, intel core, amd ryzen, 240hz',
        'Ordinateur Portable Gaming OMEN 16 | Jouez Votre Meilleur',
        'Écran jusqu''à QHD 240Hz, Refroidissement Tempest', 'omen-portable-gaming-16', 'fr', 157)
on conflict (description_id) do nothing;

-- Product 158: Apple Studio Display (Man: 19, Type: 16)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (158, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        18.8, false, 6.6, true, false, 13.9, 24.5, 'ELEC-REF-158', 22, 19,
        '65f023632bc46470c104b75f', 16) -- Type 16 (TELEVISIONS - *Monitor*)
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (315, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Apple Studio Display</h1><p>An expansive 27-inch 5K Retina display. A 12MP Ultra Wide camera with Center Stage. A studio-quality three-mic array. And a six-speaker sound system with Spatial Audio. Studio Display is more than just a display. It’s a window to new worlds.</p><h2>Key Features:</h2><ul><li>27-inch 5K Retina display (5120x2880)</li><li>12MP Ultra Wide camera with Center Stage</li><li>Studio-quality three-microphone array</li><li>Six-speaker sound system with Spatial Audio</li><li>A13 Bionic chip</li><li>Thunderbolt 3 (USB-C) port</li><li>Standard or Nano-texture glass options</li></ul><h2>Specifications:</h2><ul><li>Screen Size: 27 inches</li><li>Resolution: 5K (5120 by 2880)</li><li>Brightness: 600 nits</li><li>Camera: 12MP Ultra Wide with Center Stage</li><li>Audio: Six-speaker system, three-mic array</li></ul>',
        'Apple Studio Display', 'Apple Studio Display - 27-inch 5K Retina',
        'The Apple Studio Display features a stunning 27-inch 5K Retina screen, 12MP Center Stage camera, studio mics, and immersive six-speaker sound.',
        'apple studio display, 5k monitor, retina display, apple monitor, center stage, spatial audio',
        'Apple Studio Display | A Sight to Behold',
        '27-inch 5K Retina Display', 'apple-studio-display', 'en', 158)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (316, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Apple Studio Display</h1><p>Un écran Retina 5K immersif de 27 pouces. Une caméra ultra grand-angle 12 Mpx avec Cadre centré. Un ensemble de trois micros de qualité studio. Et un système audio à six haut-parleurs avec Audio spatial. Le Studio Display est bien plus qu’un simple écran. C’est une fenêtre ouverte sur de nouveaux mondes.</p><h2>Caractéristiques Clés :</h2><ul><li>Écran Retina 5K de 27 pouces (5120x2880)</li><li>Caméra ultra grand-angle 12 Mpx avec Cadre centré</li><li>Ensemble de trois micros de qualité studio</li><li>Système audio à six haut-parleurs avec Audio spatial</li><li>Puce A13 Bionic</li><li>Port Thunderbolt 3 (USB-C)</li><li>Options de verre standard ou nano-texturé</li></ul><h2>Spécifications :</h2><ul><li>Taille de l’écran : 27 pouces</li><li>Résolution : 5K (5120 par 2880)</li><li>Luminosité : 600 nits</li><li>Caméra : Ultra grand-angle 12 Mpx avec Cadre centré</li><li>Audio : Système à six haut-parleurs, ensemble de trois micros</li></ul>',
        'Apple Studio Display', 'Apple Studio Display - Retina 5K 27 pouces',
        'L''Apple Studio Display est doté d''un superbe écran Retina 5K de 27 pouces, d''une caméra 12 Mpx avec Cadre centré, de micros de qualité studio et d''un son immersif à six haut-parleurs.',
        'apple studio display, moniteur 5k, écran retina, moniteur apple, cadre centré, audio spatial',
        'Apple Studio Display | Un Spectacle à Voir',
        'Écran Retina 5K 27 pouces', 'apple-studio-display', 'fr', 158)
on conflict (description_id) do nothing;

-- Product 159: Samsung Galaxy Book4 Pro (Man: 20, Type: 14)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (159, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        0.49, false, 9.86, true, false, 3.44, 13.99, 'ELEC-REF-159', 23, 20,
        '65f023632bc46470c104b75f', 14) -- Type 14 (LAPTOPS)
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (317, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Samsung Galaxy Book4 Pro</h1><p>Power meets portability. The Galaxy Book4 Pro features a stunning Dynamic AMOLED 2X touchscreen, the latest Intel® Core™ Ultra processors, and seamless Galaxy ecosystem connectivity.</p><h2>Key Features:</h2><ul><li>14" or 16" Dynamic AMOLED 2X Touchscreen</li><li>Intel® Core™ Ultra processors</li><li>Intel® Arc™ graphics</li><li>Thin and light design</li><li>Vision Booster for outdoor visibility</li><li>Seamless Galaxy ecosystem integration</li><li>Windows 11</li></ul><h2>Specifications:</h2><ul><li>Display: 14"/16" Dynamic AMOLED 2X (2880x1800) Touchscreen</li><li>Processor: Intel® Core™ Ultra 5 / Ultra 7</li><li>Graphics: Intel® Arc™</li><li>RAM: 16GB LPDDR5X</li><li>Storage: 512GB / 1TB NVMe SSD</li></ul>',
        'Samsung Galaxy Book4 Pro', 'Samsung Galaxy Book4 Pro - AMOLED Touchscreen Laptop',
        'The Samsung Galaxy Book4 Pro combines a brilliant Dynamic AMOLED 2X touchscreen with powerful Intel Core Ultra performance in a thin, light package.',
        'galaxy book4 pro, samsung, laptop, windows 11, intel core ultra, amoled, touchscreen',
        'Samsung Galaxy Book4 Pro | Brilliant & Powerful',
        'Dynamic AMOLED 2X Touchscreen', 'samsung-galaxy-book4-pro', 'en', 159)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (318, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Samsung Galaxy Book4 Pro</h1><p>La puissance rencontre la portabilité. Le Galaxy Book4 Pro est doté d''un superbe écran tactile Dynamic AMOLED 2X, des derniers processeurs Intel® Core™ Ultra et d''une connectivité transparente avec l''écosystème Galaxy.</p><h2>Caractéristiques Clés :</h2><ul><li>Écran Tactile Dynamic AMOLED 2X 14" ou 16"</li><li>Processeurs Intel® Core™ Ultra</li><li>Graphiques Intel® Arc™</li><li>Design fin et léger</li><li>Vision Booster pour la visibilité extérieure</li><li>Intégration transparente de l''écosystème Galaxy</li><li>Windows 11</li></ul><h2>Spécifications :</h2><ul><li>Écran : Tactile Dynamic AMOLED 2X 14"/16" (2880x1800)</li><li>Processeur : Intel® Core™ Ultra 5 / Ultra 7</li><li>Graphiques : Intel® Arc™</li><li>RAM : 16 Go LPDDR5X</li><li>Stockage : 512 Go / 1 To SSD NVMe</li></ul>',
        'Samsung Galaxy Book4 Pro', 'Samsung Galaxy Book4 Pro - Ordinateur Portable à Écran Tactile AMOLED',
        'Le Samsung Galaxy Book4 Pro combine un brillant écran tactile Dynamic AMOLED 2X avec les puissantes performances Intel Core Ultra dans un format fin et léger.',
        'galaxy book4 pro, samsung, ordinateur portable, windows 11, intel core ultra, amoled, écran tactile',
        'Samsung Galaxy Book4 Pro | Brillant et Puissant',
        'Écran Tactile Dynamic AMOLED 2X', 'samsung-galaxy-book4-pro', 'fr', 159)
on conflict (description_id) do nothing;

-- Product 160: Sony HT-A5000 Soundbar (Man: 21, Type: 16)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (160, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        2.75, false, 5.25, true, false, 13.5, 47.75, 'ELEC-REF-160', 24, 21,
        '65f023632bc46470c104b75f', 16) -- Type 16 (TELEVISIONS - *Soundbar*)
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (319, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Sony HT-A5000 5.1.2ch Dolby Atmos® Soundbar</h1><p>Discover a new level of immersion. The HT-A5000 envelops you in authentic 5.1.2 channel surround sound with S-Force PRO Front Surround and Vertical Surround Engine.</p><h2>Key Features:</h2><ul><li>5.1.2ch Dolby Atmos®/DTS:X® surround sound</li><li>Sound Field Optimization for easy setup</li><li>Vertical Surround Engine and S-Force PRO Front Surround</li><li>Immersive Audio Enhancement</li><li>Supports 8K HDR, 4K/120fps passthrough</li><li>Hi-Res Audio capable</li></ul><h2>Specifications:</h2><ul><li>Channels: 5.1.2ch</li><li>Output Power: 450W</li><li>Sound Technologies: Dolby Atmos, DTS:X, Vertical Surround Engine, S-Force PRO</li><li>Connectivity: HDMI eARC/ARC, Optical, USB, Bluetooth, Wi-Fi</li></ul>',
        'Sony HT-A5000 Soundbar', 'Sony HT-A5000 5.1.2ch Dolby Atmos Soundbar',
        'Immerse yourself in cinematic sound with the Sony HT-A5000 soundbar, featuring 5.1.2ch Dolby Atmos, Vertical Surround Engine, and 8K/4K passthrough.',
        'sony ht-a5000, soundbar, dolby atmos, dts:x, home theater, surround sound',
        'Sony HT-A5000 Soundbar | Immersive Surround',
        '5.1.2ch Dolby Atmos', 'sony-ht-a5000-soundbar', 'en', 160)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (320, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Barre de Son Sony HT-A5000 5.1.2ch Dolby Atmos®</h1><p>Découvrez un nouveau niveau d''immersion. La HT-A5000 vous enveloppe d''un son surround authentique 5.1.2 canaux avec S-Force PRO Front Surround et Vertical Surround Engine.</p><h2>Caractéristiques Clés :</h2><ul><li>Son surround 5.1.2ch Dolby Atmos®/DTS:X®</li><li>Optimisation du champ sonore pour une configuration facile</li><li>Vertical Surround Engine et S-Force PRO Front Surround</li><li>Amélioration audio immersive</li><li>Prise en charge 8K HDR, passthrough 4K/120fps</li><li>Compatible Hi-Res Audio</li></ul><h2>Spécifications :</h2><ul><li>Canaux : 5.1.2ch</li><li>Puissance de sortie : 450W</li><li>Technologies sonores : Dolby Atmos, DTS:X, Vertical Surround Engine, S-Force PRO</li><li>Connectivité : HDMI eARC/ARC, Optique, USB, Bluetooth, Wi-Fi</li></ul>',
        'Barre de Son Sony HT-A5000', 'Barre de Son Sony HT-A5000 5.1.2ch Dolby Atmos',
        'Plongez dans un son cinématographique avec la barre de son Sony HT-A5000, dotée de Dolby Atmos 5.1.2ch, Vertical Surround Engine et passthrough 8K/4K.',
        'sony ht-a5000, barre de son, dolby atmos, dts:x, home cinéma, son surround',
        'Barre de Son Sony HT-A5000 | Surround Immersif',
        'Dolby Atmos 5.1.2ch', 'sony-ht-a5000-barre-de-son', 'fr', 160)
on conflict (description_id) do nothing;

-- Product 161: LG Tone Free FP9 Earbuds (Man: 22, Type: 15)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (161, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        null, false, null, true, false, 0.011, null, 'ELEC-REF-161', 25, 22,
        '65f023632bc46470c104b75f', 15) -- Type 15 (HEADPHONES)
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (321, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>LG TONE Free FP9 True Wireless Bluetooth UVnano Earbuds</h1><p>Immersive sound, hygienic listening. The LG TONE Free FP9 earbuds feature Meridian sound technology, Active Noise Cancellation, and a UVnano charging case that helps reduce bacteria.</p><h2>Key Features:</h2><ul><li>Sound by Meridian Technology</li><li>Active Noise Cancellation</li><li>UVnano Charging Case kills 99.9% of bacteria</li><li>Plug & Wireless feature (use case as wireless dongle)</li><li>Comfortable and hypoallergenic medical-grade ear gels</li><li>IPX4 Water-Resistant</li></ul><h2>Specifications:</h2><ul><li>Sound: Meridian Technology</li><li>ANC: Yes</li><li>UVnano Case: Yes</li><li>Battery Life: Up to 10 hours (ANC off), Up to 24 hours (with case, ANC off)</li><li>Water Resistance: IPX4 (Earbuds only)</li></ul>',
        'LG TONE Free FP9 Earbuds', 'LG TONE Free FP9 UVnano True Wireless Earbuds',
        'Enjoy premium Meridian sound and Active Noise Cancellation with LG TONE Free FP9 earbuds, featuring a bacteria-reducing UVnano case.',
        'lg tone free fp9, earbuds, true wireless, anc, noise cancelling, meridian audio, uvnano',
        'LG TONE Free FP9 | Clean & Immersive Sound',
        'UVnano Case, Meridian Sound', 'lg-tone-free-fp9-earbuds', 'en', 161)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (322, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Écouteurs Bluetooth True Wireless UVnano LG TONE Free FP9</h1><p>Son immersif, écoute hygiénique. Les écouteurs LG TONE Free FP9 sont dotés de la technologie sonore Meridian, de la Réduction active du bruit et d''un étui de charge UVnano qui aide à réduire les bactéries.</p><h2>Caractéristiques Clés :</h2><ul><li>Son par Technologie Meridian</li><li>Réduction Active du Bruit</li><li>Étui de Charge UVnano tue 99,9% des bactéries</li><li>Fonction Plug & Wireless (utiliser l''étui comme dongle sans fil)</li><li>Embouts auriculaires hypoallergéniques de qualité médicale et confortables</li><li>Résistance à l''eau IPX4</li></ul><h2>Spécifications :</h2><ul><li>Son : Technologie Meridian</li><li>ANC : Oui</li><li>Étui UVnano : Oui</li><li>Autonomie : Jusqu''à 10 heures (ANC désactivé), Jusqu''à 24 heures (avec étui, ANC désactivé)</li><li>Résistance à l''eau : IPX4 (Écouteurs uniquement)</li></ul>',
        'Écouteurs LG TONE Free FP9', 'Écouteurs True Wireless UVnano LG TONE Free FP9',
        'Profitez d''un son Meridian haut de gamme et de la Réduction active du bruit avec les écouteurs LG TONE Free FP9, dotés d''un étui UVnano réduisant les bactéries.',
        'lg tone free fp9, écouteurs, true wireless, anc, réduction de bruit, audio meridian, uvnano',
        'LG TONE Free FP9 | Son Propre et Immersif',
        'Étui UVnano, Son Meridian', 'lg-tone-free-fp9-ecouteurs', 'fr', 161)
on conflict (description_id) do nothing;

-- Product 162: Dell G15 Gaming Laptop (Man: 23, Type: 14)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (162, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        1.06, false, 10.74, true, false, 5.95, 14.07, 'ELEC-REF-162', 26, 23,
        '65f023632bc46470c104b75f', 14) -- Type 14 (LAPTOPS)
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (323, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Dell G15 Gaming Laptop</h1><p>Game on with the Dell G15 gaming laptop. Featuring powerful processors, discrete graphics, and an optimized thermal design for smooth, uninterrupted gameplay.</p><h2>Key Features:</h2><ul><li>15.6-inch FHD 120Hz or 165Hz display</li><li>Intel® Core™ or AMD Ryzen™ processors</li><li>NVIDIA® GeForce RTX™ graphics</li><li>Alienware-inspired thermal design</li><li>Game Shift technology for performance boost</li><li>Windows 11</li></ul><h2>Specifications:</h2><ul><li>Display: 15.6" FHD (1920x1080) 120Hz or 165Hz</li><li>Processor: Intel® Core™ i5/i7 or AMD Ryzen™ 5/7</li><li>Graphics: NVIDIA® GeForce RTX™ 30/40 Series</li><li>RAM: 8GB / 16GB DDR5</li><li>Storage: 256GB / 512GB / 1TB NVMe SSD</li></ul>',
        'Dell G15 Gaming Laptop', 'Dell G15 - Affordable Gaming Power',
        'The Dell G15 gaming laptop delivers reliable gaming performance with powerful hardware, effective cooling, and Game Shift technology.',
        'dell g15, gaming laptop, nvidia rtx, intel core, amd ryzen, 120hz, 165hz', 'Dell G15 Gaming Laptop | Game On',
        'Game Shift Technology', 'dell-g15-gaming-laptop', 'en', 162)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (324, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Ordinateur Portable Gaming Dell G15</h1><p>Jouez avec l''ordinateur portable gaming Dell G15. Doté de processeurs puissants, de cartes graphiques dédiées et d''une conception thermique optimisée pour un jeu fluide et ininterrompu.</p><h2>Caractéristiques Clés :</h2><ul><li>Écran FHD 15,6 pouces 120 Hz ou 165 Hz</li><li>Processeurs Intel® Core™ ou AMD Ryzen™</li><li>Cartes graphiques NVIDIA® GeForce RTX™</li><li>Conception thermique inspirée d''Alienware</li><li>Technologie Game Shift pour booster les performances</li><li>Windows 11</li></ul><h2>Spécifications :</h2><ul><li>Écran : 15,6" FHD (1920x1080) 120 Hz ou 165 Hz</li><li>Processeur : Intel® Core™ i5/i7 ou AMD Ryzen™ 5/7</li><li>Graphiques : NVIDIA® GeForce RTX™ séries 30/40</li><li>RAM : 8 Go / 16 Go DDR5</li><li>Stockage : 256 Go / 512 Go / 1 To SSD NVMe</li></ul>',
        'Ordinateur Portable Gaming Dell G15', 'Dell G15 - Puissance Gaming Abordable',
        'L''ordinateur portable gaming Dell G15 offre des performances de jeu fiables avec un matériel puissant, un refroidissement efficace et la technologie Game Shift.',
        'dell g15, portable gaming, nvidia rtx, intel core, amd ryzen, 120hz, 165hz',
        'Ordinateur Portable Gaming Dell G15 | Jouez',
        'Technologie Game Shift', 'dell-g15-portable-gaming', 'fr', 162)
on conflict (description_id) do nothing;

-- Product 163: HP Pavilion Plus 14 Laptop (Man: 24, Type: 14)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (163, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        0.72, false, 8.92, true, false, 3.09, 12.36, 'ELEC-REF-163', 27, 24,
        '65f023632bc46470c104b75f', 14) -- Type 14 (LAPTOPS)
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (325, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>HP Pavilion Plus 14 Laptop</h1><p>Experience brilliant visuals and smooth performance with the HP Pavilion Plus 14. Featuring a vibrant OLED display option and powerful processors in a sleek, portable design.</p><h2>Key Features:</h2><ul><li>14-inch display (up to 2.8K OLED)</li><li>Intel® Core™ or AMD Ryzen™ processors</li><li>Lightweight and portable design</li><li>5MP camera with HP Presence</li><li>Sustainable design with recycled materials</li><li>Windows 11</li></ul><h2>Specifications:</h2><ul><li>Display: 14" 2.2K IPS or 2.8K OLED</li><li>Processor: Intel® Core™ i5/i7 or AMD Ryzen™ 5/7</li><li>RAM: 16GB</li><li>Storage: 512GB / 1TB PCIe® NVMe™ SSD</li></ul>',
        'HP Pavilion Plus 14 Laptop', 'HP Pavilion Plus 14 - Vibrant & Portable',
        'The HP Pavilion Plus 14 laptop offers stunning visuals with an optional OLED display and reliable performance in a thin and light package.',
        'hp pavilion plus 14, laptop, oled laptop, windows 11, intel core, amd ryzen, portable laptop',
        'HP Pavilion Plus 14 | Brilliant Portability',
        'Up to 2.8K OLED Display', 'hp-pavilion-plus-14-laptop', 'en', 163)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (326, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Ordinateur Portable HP Pavilion Plus 14</h1><p>Profitez de visuels brillants et de performances fluides avec le HP Pavilion Plus 14. Doté d''une option d''écran OLED éclatant et de processeurs puissants dans un design élégant et portable.</p><h2>Caractéristiques Clés :</h2><ul><li>Écran 14 pouces (jusqu''à OLED 2.8K)</li><li>Processeurs Intel® Core™ ou AMD Ryzen™</li><li>Design léger et portable</li><li>Caméra 5MP avec HP Presence</li><li>Conception durable avec matériaux recyclés</li><li>Windows 11</li></ul><h2>Spécifications :</h2><ul><li>Écran : 14" 2.2K IPS ou 2.8K OLED</li><li>Processeur : Intel® Core™ i5/i7 ou AMD Ryzen™ 5/7</li><li>RAM : 16 Go</li><li>Stockage : 512 Go / 1 To SSD PCIe® NVMe™</li></ul>',
        'Ordinateur Portable HP Pavilion Plus 14', 'HP Pavilion Plus 14 - Éclatant et Portable',
        'L''ordinateur portable HP Pavilion Plus 14 offre des visuels époustouflants avec un écran OLED en option et des performances fiables dans un format fin et léger.',
        'hp pavilion plus 14, ordinateur portable, portable oled, windows 11, intel core, amd ryzen, portable léger',
        'HP Pavilion Plus 14 | Portabilité Brillante',
        'Écran jusqu''à OLED 2.8K', 'hp-pavilion-plus-14-portable', 'fr', 163)
on conflict (description_id) do nothing;

-- Product 164: Apple Beats Studio Pro Headphones (Man: 19, Type: 15)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (164, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        null, false, null, true, false, 0.57, null, 'ELEC-REF-164', 28, 19,
        '65f023632bc46470c104b75f', 15) -- Type 15 (HEADPHONES)
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (327, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Beats Studio Pro - Wireless Noise Cancelling Headphones</h1><p>Immerse yourself in sound with Beats Studio Pro. Featuring fully adaptive Active Noise Cancelling, Personalized Spatial Audio, and lossless audio capability via USB-C.</p><h2>Key Features:</h2><ul><li>Fully adaptive Active Noise Cancelling (ANC)</li><li>Transparency mode</li><li>Personalized Spatial Audio with dynamic head tracking</li><li>Lossless Audio via USB-C</li><li>Up to 40 hours of battery life</li><li>Enhanced Apple and Android compatibility</li></ul><h2>Specifications:</h2><ul><li>Connectivity: Bluetooth 5.3, USB-C, 3.5mm audio cable</li><li>Battery Life: Up to 40 hours (ANC off), Up to 24 hours (ANC on)</li><li>Weight: 260g</li></ul>',
        'Beats Studio Pro Headphones', 'Beats Studio Pro Wireless Noise Cancelling Headphones',
        'Experience immersive listening with Beats Studio Pro headphones, offering adaptive ANC, Spatial Audio, and lossless audio support.',
        'beats studio pro, headphones, noise cancelling, wireless, bluetooth, spatial audio, apple, beats by dre',
        'Beats Studio Pro | Immersive Sound',
        'Adaptive ANC, Lossless Audio', 'beats-studio-pro-headphones', 'en', 164)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (328, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Beats Studio Pro - Casque sans Fil à Réduction de Bruit</h1><p>Plongez au cœur du son avec le Beats Studio Pro. Doté de la Réduction active du bruit entièrement adaptative, de l’Audio spatial personnalisé et de la capacité audio sans perte via USB-C.</p><h2>Caractéristiques Clés :</h2><ul><li>Réduction active du bruit (ANC) entièrement adaptative</li><li>Mode Transparence</li><li>Audio spatial personnalisé avec suivi dynamique de la tête</li><li>Audio sans perte via USB-C</li><li>Jusqu’à 40 heures d’autonomie</li><li>Compatibilité améliorée avec Apple et Android</li></ul><h2>Spécifications :</h2><ul><li>Connectivité : Bluetooth 5.3, USB-C, câble audio 3,5 mm</li><li>Autonomie : Jusqu’à 40 heures (ANC désactivé), Jusqu’à 24 heures (ANC activé)</li><li>Poids : 260 g</li></ul>',
        'Casque Beats Studio Pro', 'Casque sans Fil à Réduction de Bruit Beats Studio Pro',
        'Vivez une écoute immersive avec le casque Beats Studio Pro, offrant une ANC adaptative, l''Audio spatial et la prise en charge de l''audio sans perte.',
        'beats studio pro, casque, réduction de bruit, sans fil, bluetooth, audio spatial, apple, beats by dre',
        'Beats Studio Pro | Son Immersif',
        'ANC Adaptative, Audio Sans Perte', 'beats-studio-pro-casque', 'fr', 164)
on conflict (description_id) do nothing;

-- Product 165: Samsung Galaxy S23 FE (Man: 20, Type: 13)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (165, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        6.22, false, 3.01, true, false, 0.46, 0.32, 'ELEC-REF-165', 29, 20,
        '65f023632bc46470c104b75f', 13) -- Type 13 (SMARTPHONES)
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (329, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Samsung Galaxy S23 FE</h1><p>The epic starts here. Galaxy S23 FE brings fan-favorite features like a pro-grade camera, powerful processor, and a smooth display into a premium, iconic design.</p><h2>Key Features:</h2><ul><li>6.4-inch Dynamic AMOLED 2X 120Hz display</li><li>Pro-grade Camera System (50MP Main)</li><li>Powerful Processor (Snapdragon® 8 Gen 1 / Exynos 2200)</li><li>Premium Design with Gorilla Glass 5</li><li>All-day Battery</li><li>IP68 Water & Dust Resistance</li></ul><h2>Specifications:</h2><ul><li>Display: 6.4" FHD+ Dynamic AMOLED 2X, 120Hz</li><li>Processor: Snapdragon® 8 Gen 1 or Exynos 2200 (Varies by region)</li><li>RAM: 8GB</li><li>Storage: 128GB / 256GB</li><li>Battery: 4,500mAh</li></ul>',
        'Samsung Galaxy S23 FE', 'Samsung Galaxy S23 FE - Fan Edition Power',
        'Get flagship features with the Samsung Galaxy S23 FE, including a pro-grade camera, powerful performance, and a vibrant 120Hz display.',
        'galaxy s23 fe, samsung, smartphone, android, fan edition, snapdragon 8 gen 1, exynos 2200',
        'Samsung Galaxy S23 FE | Epic Features',
        'Pro-Grade Camera, 120Hz Display', 'samsung-galaxy-s23-fe', 'en', 165)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (330, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Samsung Galaxy S23 FE</h1><p>L''épique commence ici. Le Galaxy S23 FE réunit les fonctionnalités préférées des fans comme un appareil photo de qualité professionnelle, un processeur puissant et un écran fluide dans un design premium et iconique.</p><h2>Caractéristiques Clés :</h2><ul><li>Écran Dynamic AMOLED 2X 120Hz 6,4 pouces</li><li>Système photo de qualité pro (Principal 50 Mpx)</li><li>Processeur Puissant (Snapdragon® 8 Gen 1 / Exynos 2200)</li><li>Design Premium avec Gorilla Glass 5</li><li>Batterie pour toute la journée</li><li>Résistance à l''eau et à la poussière IP68</li></ul><h2>Spécifications :</h2><ul><li>Écran : 6,4" FHD+ Dynamic AMOLED 2X, 120Hz</li><li>Processeur : Snapdragon® 8 Gen 1 ou Exynos 2200 (Varie selon la région)</li><li>RAM : 8 Go</li><li>Stockage : 128 Go / 256 Go</li><li>Batterie : 4 500 mAh</li></ul>',
        'Samsung Galaxy S23 FE', 'Samsung Galaxy S23 FE - Puissance Fan Edition',
        'Obtenez des fonctionnalités phares avec le Samsung Galaxy S23 FE, y compris un appareil photo de qualité pro, des performances puissantes et un écran 120Hz éclatant.',
        'galaxy s23 fe, samsung, smartphone, android, fan edition, snapdragon 8 gen 1, exynos 2200',
        'Samsung Galaxy S23 FE | Fonctionnalités Épiques',
        'Caméra Pro, Écran 120Hz', 'samsung-galaxy-s23-fe', 'fr', 165)
on conflict (description_id) do nothing;
-- Product 166: LG OLED evo C3 55-inch TV (Man: 22, Type: 16)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (166, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        27.7, false, 1.8, true, false, 32.6, 48.2, 'ELEC-REF-166', 30, 22,
        '65f023632bc46470c104b75f', 16) -- Type 16 (TELEVISIONS)
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (331, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>LG OLED evo C3 55-inch 4K Smart TV</h1><p>Experience the next generation of OLED with the LG C3. Featuring the α9 AI Processor Gen6 for enhanced picture and sound, Brightness Booster for vivid visuals, and seamless smart features.</p><h2>Key Features:</h2><ul><li>55-inch OLED evo Display</li><li>α9 AI Processor Gen6</li><li>Brightness Booster</li><li>Dolby Vision IQ & Dolby Atmos</li><li>webOS 23 Smart Platform</li><li>Ultimate Gaming: 120Hz, VRR, G-SYNC, FreeSync</li></ul><h2>Specifications:</h2><ul><li>Screen Size: 55 inches</li><li>Resolution: 4K UHD (3840 x 2160)</li><li>Display Technology: OLED evo</li><li>Processor: α9 AI Processor Gen6</li><li>Refresh Rate: 120Hz Native</li></ul>',
        'LG OLED evo C3 55-inch TV', 'LG 55" Class C3 Series OLED 4K UHD Smart webOS TV',
        'The LG OLED evo C3 55-inch TV offers breathtaking picture quality, AI-powered processing, and advanced gaming features.',
        'lg c3, oled tv, 55 inch tv, 4k smart tv, webos 23, dolby vision, gaming tv',
        'LG OLED evo C3 55" TV | Stunning Visuals',
        'OLED evo, α9 AI Processor', 'lg-oled-evo-c3-55-inch-tv', 'en', 166)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (332, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Téléviseur Intelligent LG OLED evo C3 55 pouces 4K</h1><p>Découvrez la nouvelle génération d''OLED avec le LG C3. Doté du processeur α9 AI Gen6 pour une image et un son améliorés, du Brightness Booster pour des visuels éclatants et de fonctionnalités intelligentes fluides.</p><h2>Caractéristiques Clés :</h2><ul><li>Écran OLED evo 55 pouces</li><li>Processeur α9 AI Gen6</li><li>Brightness Booster</li><li>Dolby Vision IQ & Dolby Atmos</li><li>Plateforme Intelligente webOS 23</li><li>Jeu Ultime : 120Hz, VRR, G-SYNC, FreeSync</li></ul><h2>Spécifications :</h2><ul><li>Taille de l''écran : 55 pouces</li><li>Résolution : 4K UHD (3840 x 2160)</li><li>Technologie d''affichage : OLED evo</li><li>Processeur : α9 AI Processor Gen6</li><li>Taux de rafraîchissement : 120Hz Natif</li></ul>',
        'Téléviseur LG OLED evo C3 55 pouces', 'Téléviseur Intelligent LG 55" Classe Série C3 OLED 4K UHD webOS',
        'Le téléviseur LG OLED evo C3 55 pouces offre une qualité d''image époustouflante, un traitement assisté par IA et des fonctionnalités de jeu avancées.',
        'lg c3, tv oled, tv 55 pouces, smart tv 4k, webos 23, dolby vision, tv gaming',
        'Téléviseur LG OLED evo C3 55" | Visuels Stupéfiants',
        'OLED evo, Processeur α9 AI', 'lg-oled-evo-c3-55-pouces-tv', 'fr', 166)
on conflict (description_id) do nothing;

-- Product 167: Dell UltraSharp 27 Monitor - U2723QE (Man: 23, Type: 16)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (167, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        15.2, false, 7.3, true, false, 9.8, 24.1, 'ELEC-REF-167', 31, 23,
        '65f023632bc46470c104b75f', 16) -- Type 16 (TELEVISIONS - *Monitor*)
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (333, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Dell UltraSharp 27 4K USB-C Hub Monitor - U2723QE</h1><p>Experience outstanding color and productivity with this 27-inch 4K monitor featuring IPS Black technology and extensive connectivity, including a USB-C hub.</p><h2>Key Features:</h2><ul><li>27-inch 4K UHD (3840x2160) IPS Black Display</li><li>Exceptional contrast (2000:1) and color accuracy (98% DCI-P3)</li><li>ComfortView Plus low blue light screen</li><li>Extensive connectivity: USB-C (90W Power Delivery), RJ45 Ethernet, HDMI, DisplayPort</li><li>Multi-tasking features (KVM, Picture-by-Picture, Picture-in-Picture)</li></ul><h2>Specifications:</h2><ul><li>Screen Size: 27 inches</li><li>Resolution: 4K UHD (3840 x 2160)</li><li>Panel Type: IPS Black</li><li>Contrast Ratio: 2000:1</li><li>Connectivity: USB-C Hub, HDMI, DP, RJ45</li></ul>',
        'Dell UltraSharp 27 4K Monitor U2723QE', 'Dell UltraSharp 27 4K USB-C Hub Monitor - U2723QE',
        'Boost productivity with the Dell U2723QE 27-inch 4K monitor, offering stunning visuals with IPS Black, wide color coverage, and USB-C hub connectivity.',
        'dell u2723qe, 4k monitor, 27 inch monitor, ips black, usb-c hub, professional monitor',
        'Dell UltraSharp 27 4K Monitor | Productivity Hub',
        'IPS Black, USB-C Hub', 'dell-ultrasharp-27-4k-monitor-u2723qe', 'en', 167)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (334, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Moniteur Hub USB-C Dell UltraSharp 27 4K - U2723QE</h1><p>Profitez de couleurs et d''une productivité exceptionnelles avec ce moniteur 4K de 27 pouces doté de la technologie IPS Black et d''une connectivité étendue, y compris un hub USB-C.</p><h2>Caractéristiques Clés :</h2><ul><li>Écran IPS Black 4K UHD 27 pouces (3840x2160)</li><li>Contraste exceptionnel (2000:1) et précision des couleurs (98% DCI-P3)</li><li>Écran ComfortView Plus à faible lumière bleue</li><li>Connectivité étendue : USB-C (Power Delivery 90W), Ethernet RJ45, HDMI, DisplayPort</li><li>Fonctionnalités multitâches (KVM, Picture-by-Picture, Picture-in-Picture)</li></ul><h2>Spécifications :</h2><ul><li>Taille de l''écran : 27 pouces</li><li>Résolution : 4K UHD (3840 x 2160)</li><li>Type de dalle : IPS Black</li><li>Rapport de contraste : 2000:1</li><li>Connectivité : Hub USB-C, HDMI, DP, RJ45</li></ul>',
        'Moniteur Dell UltraSharp 27 4K U2723QE', 'Moniteur Hub USB-C Dell UltraSharp 27 4K - U2723QE',
        'Augmentez votre productivité avec le moniteur Dell U2723QE 27 pouces 4K, offrant des visuels époustouflants avec IPS Black, une large couverture colorimétrique et une connectivité hub USB-C.',
        'dell u2723qe, moniteur 4k, moniteur 27 pouces, ips black, hub usb-c, moniteur professionnel',
        'Moniteur Dell UltraSharp 27 4K | Hub de Productivité',
        'IPS Black, Hub USB-C', 'dell-ultrasharp-27-4k-moniteur-u2723qe', 'fr', 167)
on conflict (description_id) do nothing;

-- Product 168: Sony WF-1000XM5 Earbuds (Man: 21, Type: 15)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (168, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        null, false, null, true, false, 0.013, null, 'ELEC-REF-168', 32, 21,
        '65f023632bc46470c104b75f', 15) -- Type 15 (HEADPHONES)
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (335, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Sony WF-1000XM5 The Best Truly Wireless Noise Canceling Earbuds</h1><p>Experience the best noise canceling performance yet with the Sony WF-1000XM5. Featuring the new Integrated Processor V2 and HD Noise Canceling Processor QN2e for unparalleled silence and stunning sound quality.</p><h2>Key Features:</h2><ul><li>The Best Noise Canceling performance</li><li>New Integrated Processor V2 & HD Noise Canceling Processor QN2e</li><li>Astonishing sound quality with High-Resolution Audio Wireless</li><li>Sony’s best-ever call quality with AI noise reduction</li><li>Small, light, and beautifully designed</li><li>Up to 24 hours battery life with charging case</li></ul><h2>Specifications:</h2><ul><li>Weight: Approx. 5.9 g x 2</li><li>Driver Unit: Dynamic Driver X 8.4 mm</li><li>Noise Canceling: Yes, Industry Leading</li><li>Battery Life: Max. 8 hours (NC On), Max. 12 hours (NC Off)</li><li>Water Resistance: IPX4</li></ul>',
        'Sony WF-1000XM5 Earbuds', 'Sony WF-1000XM5 Truly Wireless Noise Canceling Earbuds',
        'Sony WF-1000XM5 earbuds deliver the best noise canceling, astonishing sound quality, and crystal-clear calls in a compact design.',
        'sony wf-1000xm5, earbuds, true wireless, noise cancelling, hi-res audio, sony earbuds',
        'Sony WF-1000XM5 | The Best Silence',
        'Best Noise Canceling', 'sony-wf-1000xm5-earbuds', 'en', 168)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (336, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Écouteurs Sony WF-1000XM5, les Meilleurs Écouteurs True Wireless à Réduction de Bruit</h1><p>Découvrez les meilleures performances de réduction de bruit à ce jour avec les Sony WF-1000XM5. Dotés du nouveau processeur intégré V2 et du processeur HD de réduction de bruit QN2e pour un silence inégalé et une qualité sonore stupéfiante.</p><h2>Caractéristiques Clés :</h2><ul><li>Les Meilleures performances de Réduction de Bruit</li><li>Nouveau Processeur Intégré V2 & Processeur HD de Réduction de Bruit QN2e</li><li>Qualité sonore étonnante avec Hi-Resolution Audio Wireless</li><li>La meilleure qualité d''appel jamais conçue par Sony avec réduction du bruit par IA</li><li>Petits, légers et magnifiquement conçus</li><li>Jusqu''à 24 heures d''autonomie avec l''étui de charge</li></ul><h2>Spécifications :</h2><ul><li>Poids : Env. 5,9 g x 2</li><li>Diaphragme : Dynamic Driver X 8,4 mm</li><li>Réduction de bruit : Oui, Leader du marché</li><li>Autonomie : Max. 8 heures (NC On), Max. 12 heures (NC Off)</li><li>Résistance à l''eau : IPX4</li></ul>',
        'Écouteurs Sony WF-1000XM5', 'Écouteurs True Wireless à Réduction de Bruit Sony WF-1000XM5',
        'Les écouteurs Sony WF-1000XM5 offrent la meilleure réduction de bruit, une qualité sonore étonnante et des appels cristallins dans un design compact.',
        'sony wf-1000xm5, écouteurs, true wireless, réduction de bruit, audio hi-res, écouteurs sony',
        'Sony WF-1000XM5 | Le Meilleur Silence',
        'Meilleure Réduction de Bruit', 'sony-wf-1000xm5-ecouteurs', 'fr', 168)
on conflict (description_id) do nothing;

-- Product 169: Apple Watch Series 9 (Man: 19, Type: 13)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (169, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        1.77, false, 0.42, true, false, 0.07, 1.5, 'ELEC-REF-169', 33, 19,
        '65f023632bc46470c104b75f', 13) -- Type 13 (SMARTPHONES - *Smartwatch*)
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (337, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Apple Watch Series 9</h1><p>Smarter. Brighter. Mightier. Apple Watch Series 9 features the powerful S9 SiP, a magical new way to interact with your watch without touching the screen, and a display that’s twice as bright.</p><h2>Key Features:</h2><ul><li>Powerful S9 SiP chip</li><li>Double tap gesture for interaction</li><li>Brighter Always-On Retina display</li><li>Advanced health features: Blood Oxygen, ECG apps</li><li>Crash Detection and Fall Detection</li><li>Carbon neutral options available</li><li>watchOS 10</li></ul><h2>Specifications:</h2><ul><li>Case Sizes: 41mm, 45mm</li><li>Chip: S9 SiP with 64-bit dual-core processor</li><li>Display: Always-On Retina LTPO OLED display, up to 2000 nits</li><li>Sensors: Blood oxygen sensor, electrical heart sensor, optical heart sensor</li><li>Water Resistance: 50 meters</li></ul>',
        'Apple Watch Series 9', 'Apple Watch Series 9 - Smarter, Brighter',
        'The Apple Watch Series 9 is more powerful than ever with the S9 SiP, double tap gesture, a brighter display, and advanced health features.',
        'apple watch series 9, smartwatch, s9 sip, double tap, watchos 10, health tracking',
        'Apple Watch Series 9 | Simply Powerful',
        'S9 SiP, Double Tap Gesture', 'apple-watch-series-9', 'en', 169)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (338, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Apple Watch Series 9</h1><p>Plus intelligente. Plus brillante. Plus puissante. L’Apple Watch Series 9 est dotée de la puissante puce S9 SiP, d’une nouvelle façon magique d’interagir avec votre montre sans toucher l’écran, et d’un écran deux fois plus lumineux.</p><h2>Caractéristiques Clés :</h2><ul><li>Puissante puce S9 SiP</li><li>Geste toucher deux fois pour l''interaction</li><li>Écran Retina toujours activé plus lumineux</li><li>Fonctionnalités de santé avancées : apps Oxygène sanguin, ECG</li><li>Détection des accidents et Détection des chutes</li><li>Options neutres en carbone disponibles</li><li>watchOS 10</li></ul><h2>Spécifications :</h2><ul><li>Tailles de boîtier : 41 mm, 45 mm</li><li>Puce : S9 SiP avec processeur bicœur 64 bits</li><li>Écran : Écran Retina LTPO OLED toujours activé, jusqu’à 2 000 nits</li><li>Capteurs : Capteur d’oxygène sanguin, capteur électrique de fréquence cardiaque, capteur optique de fréquence cardiaque</li><li>Résistance à l’eau : 50 mètres</li></ul>',
        'Apple Watch Series 9', 'Apple Watch Series 9 - Plus Intelligente, Plus Brillante',
        'L''Apple Watch Series 9 est plus puissante que jamais avec la puce S9 SiP, le geste toucher deux fois, un écran plus lumineux et des fonctionnalités de santé avancées.',
        'apple watch series 9, smartwatch, puce s9, toucher deux fois, watchos 10, suivi santé',
        'Apple Watch Series 9 | Simplement Puissante',
        'Puce S9 SiP, Geste Toucher Deux Fois', 'apple-watch-series-9', 'fr', 169)
on conflict (description_id) do nothing;

-- Product 170: Samsung Galaxy Tab S9 (Man: 20, Type: 14)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (170, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        10.01, false, 0.23, true, false, 1.1, 6.51, 'ELEC-REF-170', 34, 20,
        '65f023632bc46470c104b75f', 14) -- Type 14 (LAPTOPS - *Tablet*)
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (339, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Samsung Galaxy Tab S9</h1><p>Set a new standard for tablets with the Galaxy Tab S9. Featuring a stunning Dynamic AMOLED 2X display, the powerful Snapdragon® 8 Gen 2 for Galaxy processor, and an included S Pen.</p><h2>Key Features:</h2><ul><li>11-inch Dynamic AMOLED 2X Display (120Hz)</li><li>Snapdragon® 8 Gen 2 for Galaxy processor</li><li>Included S Pen (IP68 Water & Dust Resistant)</li><li>Armor Aluminum frame</li><li>Vision Booster for outdoor clarity</li><li>Quad speakers tuned by AKG with Dolby Atmos</li><li>Android OS</li></ul><h2>Specifications:</h2><ul><li>Display: 11" Dynamic AMOLED 2X, 120Hz</li><li>Processor: Snapdragon® 8 Gen 2 for Galaxy</li><li>RAM: 8GB / 12GB</li><li>Storage: 128GB / 256GB (expandable via microSD)</li><li>Battery: 8,400mAh</li></ul>',
        'Samsung Galaxy Tab S9', 'Samsung Galaxy Tab S9 - Dynamic AMOLED 2X Tablet',
        'The Samsung Galaxy Tab S9 features a brilliant 11-inch Dynamic AMOLED 2X display, powerful Snapdragon performance, and an included S Pen for productivity and creativity.',
        'galaxy tab s9, samsung, tablet, android tablet, dynamic amoled 2x, s pen, snapdragon 8 gen 2',
        'Samsung Galaxy Tab S9 | Brilliant Display',
        'Dynamic AMOLED 2X, Included S Pen', 'samsung-galaxy-tab-s9', 'en', 170)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (340, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Samsung Galaxy Tab S9</h1><p>Établissez une nouvelle norme pour les tablettes avec la Galaxy Tab S9. Dotée d''un superbe écran Dynamic AMOLED 2X, du puissant processeur Snapdragon® 8 Gen 2 pour Galaxy et d''un S Pen inclus.</p><h2>Caractéristiques Clés :</h2><ul><li>Écran Dynamic AMOLED 2X 11 pouces (120Hz)</li><li>Processeur Snapdragon® 8 Gen 2 pour Galaxy</li><li>S Pen inclus (Résistant à l''eau et à la poussière IP68)</li><li>Cadre en Armor Aluminum</li><li>Vision Booster pour la clarté extérieure</li><li>Quatre haut-parleurs AKG avec Dolby Atmos</li><li>Système d''exploitation Android</li></ul><h2>Spécifications :</h2><ul><li>Écran : 11" Dynamic AMOLED 2X, 120Hz</li><li>Processeur : Snapdragon® 8 Gen 2 pour Galaxy</li><li>RAM : 8 Go / 12 Go</li><li>Stockage : 128 Go / 256 Go (extensible via microSD)</li><li>Batterie : 8 400 mAh</li></ul>',
        'Samsung Galaxy Tab S9', 'Samsung Galaxy Tab S9 - Tablette Dynamic AMOLED 2X',
        'La Samsung Galaxy Tab S9 est dotée d''un brillant écran Dynamic AMOLED 2X de 11 pouces, de performances Snapdragon puissantes et d''un S Pen inclus pour la productivité et la créativité.',
        'galaxy tab s9, samsung, tablette, tablette android, dynamic amoled 2x, s pen, snapdragon 8 gen 2',
        'Samsung Galaxy Tab S9 | Écran Brillant',
        'Dynamic AMOLED 2X, S Pen Inclus', 'samsung-galaxy-tab-s9', 'fr', 170)
on conflict (description_id) do nothing;

-- Product 171: HP Chromebook x360 14c (Man: 24, Type: 14)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (171, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        0.71, false, 8.08, true, false, 3.35, 12.7, 'ELEC-REF-171', 35, 24,
        '65f023632bc46470c104b75f', 14) -- Type 14 (LAPTOPS)
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (341, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>HP Chromebook x360 14c</h1><p>Flexibility meets performance. The HP Chromebook x360 14c offers a versatile 360° hinge, a responsive touchscreen, and the speed and simplicity of ChromeOS.</p><h2>Key Features:</h2><ul><li>14-inch FHD Touchscreen Display</li><li>Intel® Core™ i3 processor</li><li>360° hinge for laptop, tablet, tent modes</li><li>Fast and secure ChromeOS</li><li>Fingerprint reader</li><li>Wi-Fi 6 connectivity</li><li>Long battery life</li></ul><h2>Specifications:</h2><ul><li>Display: 14" FHD (1920x1080) IPS Touchscreen</li><li>Processor: Intel® Core™ i3</li><li>RAM: 8GB LPDDR4x</li><li>Storage: 128GB / 256GB PCIe® NVMe™ SSD</li><li>Operating System: ChromeOS</li></ul>',
        'HP Chromebook x360 14c', 'HP Chromebook x360 14c - Convertible ChromeOS Laptop',
        'The HP Chromebook x360 14c is a versatile convertible laptop with a touchscreen, Intel Core i3 power, and the secure ChromeOS experience.',
        'hp chromebook x360 14c, chromebook, convertible laptop, chromeos, touchscreen, intel core i3',
        'HP Chromebook x360 14c | Flexible & Fast',
        '360° Hinge, ChromeOS', 'hp-chromebook-x360-14c', 'en', 171)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (342, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>HP Chromebook x360 14c</h1><p>La flexibilité rencontre la performance. Le HP Chromebook x360 14c offre une charnière polyvalente à 360°, un écran tactile réactif, ainsi que la vitesse et la simplicité de ChromeOS.</p><h2>Caractéristiques Clés :</h2><ul><li>Écran Tactile FHD 14 pouces</li><li>Processeur Intel® Core™ i3</li><li>Charnière à 360° pour modes portable, tablette, tente</li><li>ChromeOS rapide et sécurisé</li><li>Lecteur d''empreintes digitales</li><li>Connectivité Wi-Fi 6</li><li>Longue autonomie</li></ul><h2>Spécifications :</h2><ul><li>Écran : Tactile IPS 14" FHD (1920x1080)</li><li>Processeur : Intel® Core™ i3</li><li>RAM : 8 Go LPDDR4x</li><li>Stockage : 128 Go / 256 Go SSD PCIe® NVMe™</li><li>Système d''exploitation : ChromeOS</li></ul>',
        'HP Chromebook x360 14c', 'HP Chromebook x360 14c - Ordinateur Portable Convertible ChromeOS',
        'Le HP Chromebook x360 14c est un ordinateur portable convertible polyvalent avec un écran tactile, la puissance Intel Core i3 et l''expérience sécurisée de ChromeOS.',
        'hp chromebook x360 14c, chromebook, portable convertible, chromeos, écran tactile, intel core i3',
        'HP Chromebook x360 14c | Flexible et Rapide',
        'Charnière 360°, ChromeOS', 'hp-chromebook-x360-14c', 'fr', 171)
on conflict (description_id) do nothing;

-- Product 172: Samsung Galaxy Z Fold5 (Man: 20, Type: 13)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (172, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        6.1, false, 0.53, true, false, 0.56, 5.11, 'ELEC-REF-172', 36, 20,
        '65f023632bc46470c104b75f', 13) -- Type 13 (SMARTPHONES)
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (343, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Samsung Galaxy Z Fold5</h1><p>Unfold an immersive screen. The Galaxy Z Fold5 features a massive 7.6-inch internal display, PC-like multitasking capabilities, and the powerful Snapdragon® 8 Gen 2 for Galaxy processor.</p><h2>Key Features:</h2><ul><li>7.6-inch Main & 6.2-inch Cover Dynamic AMOLED 2X Displays (120Hz)</li><li>Snapdragon® 8 Gen 2 for Galaxy processor</li><li>Advanced multitasking with Taskbar</li><li>S Pen support on main screen (S Pen sold separately)</li><li>Flex Hinge for durability and slim design</li><li>Pro-grade cameras</li></ul><h2>Specifications:</h2><ul><li>Main Display: 7.6" QXGA+ Dynamic AMOLED 2X, 120Hz</li><li>Cover Display: 6.2" HD+ Dynamic AMOLED 2X, 120Hz</li><li>Processor: Snapdragon® 8 Gen 2 for Galaxy</li><li>RAM: 12GB</li><li>Storage: 256GB / 512GB / 1TB</li><li>Battery: 4,400mAh</li></ul>',
        'Samsung Galaxy Z Fold5', 'Samsung Galaxy Z Fold5 - Foldable Smartphone',
        'Experience the ultimate foldable phone with the Samsung Galaxy Z Fold5, featuring a large immersive screen, powerful performance, and enhanced multitasking.',
        'galaxy z fold5, samsung, foldable phone, smartphone, android, snapdragon 8 gen 2, multitasking',
        'Samsung Galaxy Z Fold5 | Unfold Your World',
        '7.6" Foldable Display, Snapdragon 8 Gen 2', 'samsung-galaxy-z-fold5', 'en', 172)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (344, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Samsung Galaxy Z Fold5</h1><p>Dépliez un écran immersif. Le Galaxy Z Fold5 est doté d''un immense écran interne de 7,6 pouces, de capacités multitâches dignes d''un PC et du puissant processeur Snapdragon® 8 Gen 2 pour Galaxy.</p><h2>Caractéristiques Clés :</h2><ul><li>Écrans Dynamic AMOLED 2X 7,6" (principal) & 6,2" (couverture) (120Hz)</li><li>Processeur Snapdragon® 8 Gen 2 pour Galaxy</li><li>Multitâche avancé avec barre des tâches</li><li>Prise en charge du S Pen sur l''écran principal (S Pen vendu séparément)</li><li>Charnière Flex pour la durabilité et un design fin</li><li>Appareils photo de qualité professionnelle</li></ul><h2>Spécifications :</h2><ul><li>Écran principal : 7,6" QXGA+ Dynamic AMOLED 2X, 120Hz</li><li>Écran de couverture : 6,2" HD+ Dynamic AMOLED 2X, 120Hz</li><li>Processeur : Snapdragon® 8 Gen 2 pour Galaxy</li><li>RAM : 12 Go</li><li>Stockage : 256 Go / 512 Go / 1 To</li><li>Batterie : 4 400 mAh</li></ul>',
        'Samsung Galaxy Z Fold5', 'Samsung Galaxy Z Fold5 - Smartphone Pliable',
        'Découvrez le téléphone pliable ultime avec le Samsung Galaxy Z Fold5, doté d''un grand écran immersif, de performances puissantes et d''un multitâche amélioré.',
        'galaxy z fold5, samsung, téléphone pliable, smartphone, android, snapdragon 8 gen 2, multitâche',
        'Samsung Galaxy Z Fold5 | Dépliez Votre Monde',
        'Écran Pliable 7.6", Snapdragon 8 Gen 2', 'samsung-galaxy-z-fold5', 'fr', 172)
on conflict (description_id) do nothing;

-- Product 173: LG UltraFine 32" 4K Monitor (Man: 22, Type: 16)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (173, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        19.4, false, 9.1, true, false, 14.1, 28.1, 'ELEC-REF-173', 37, 22,
        '65f023632bc46470c104b75f', 16) -- Type 16 (TELEVISIONS - *Monitor*)
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (345, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>LG UltraFine™ Display 32UQ85R-W 32" 4K UHD Monitor</h1><p>Designed for creative professionals, the LG UltraFine 32UQ85R-W monitor delivers exceptional 4K resolution, accurate color reproduction with IPS Black technology, and versatile connectivity.</p><h2>Key Features:</h2><ul><li>31.5-inch 4K UHD (3840 x 2160) IPS Black Display</li><li>DCI-P3 98% Color Gamut with VESA DisplayHDR™ 400</li><li>Hardware Calibration Ready</li><li>USB Type-C™ with 90W Power Delivery</li><li>Ergonomic Stand (Height, Pivot, Tilt)</li></ul><h2>Specifications:</h2><ul><li>Screen Size: 31.5 inches</li><li>Resolution: 4K UHD (3840 x 2160)</li><li>Panel Type: IPS Black</li><li>Color Gamut: DCI-P3 98% (Typ.)</li><li>Connectivity: USB-C (90W PD), HDMI, DisplayPort</li></ul>',
        'LG UltraFine 32" 4K Monitor 32UQ85R-W', 'LG UltraFine Display 32UQ85R-W 32" 4K UHD IPS Black Monitor',
        'The LG UltraFine 32UQ85R-W is a 32-inch 4K monitor ideal for creators, featuring IPS Black, wide color gamut, and USB-C connectivity.',
        'lg ultrafine, 32uq85r-w, 4k monitor, 32 inch monitor, ips black, professional monitor, usb-c',
        'LG UltraFine 32" 4K Monitor | Creator Ready',
        '32" 4K IPS Black, DCI-P3 98%', 'lg-ultrafine-32-4k-monitor-32uq85r-w', 'en', 173)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (346, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Moniteur LG UltraFine™ Display 32UQ85R-W 32" 4K UHD</h1><p>Conçu pour les professionnels de la création, le moniteur LG UltraFine 32UQ85R-W offre une résolution 4K exceptionnelle, une reproduction fidèle des couleurs grâce à la technologie IPS Black et une connectivité polyvalente.</p><h2>Caractéristiques Clés :</h2><ul><li>Écran IPS Black 4K UHD 31,5 pouces (3840 x 2160)</li><li>Gamme de couleurs DCI-P3 98% avec VESA DisplayHDR™ 400</li><li>Prêt pour l''étalonnage matériel</li><li>USB Type-C™ avec Power Delivery 90W</li><li>Support Ergonomique (Hauteur, Pivot, Inclinaison)</li></ul><h2>Spécifications :</h2><ul><li>Taille de l''écran : 31,5 pouces</li><li>Résolution : 4K UHD (3840 x 2160)</li><li>Type de dalle : IPS Black</li><li>Gamme de couleurs : DCI-P3 98% (Typ.)</li><li>Connectivité : USB-C (90W PD), HDMI, DisplayPort</li></ul>',
        'Moniteur LG UltraFine 32" 4K 32UQ85R-W', 'Moniteur LG UltraFine Display 32UQ85R-W 32" 4K UHD IPS Black',
        'Le LG UltraFine 32UQ85R-W est un moniteur 4K de 32 pouces idéal pour les créateurs, doté d''IPS Black, d''une large gamme de couleurs et d''une connectivité USB-C.',
        'lg ultrafine, 32uq85r-w, moniteur 4k, moniteur 32 pouces, ips black, moniteur professionnel, usb-c',
        'Moniteur LG UltraFine 32" 4K | Prêt pour la Création',
        '32" 4K IPS Black, DCI-P3 98%', 'lg-ultrafine-32-4k-moniteur-32uq85r-w', 'fr', 173)
on conflict (description_id) do nothing;

-- Product 174: Apple iPad Air (M2) (Man: 19, Type: 14)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (174, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000', true, '2024-05-15 00:00:00.000000',
        true, -- Assuming pre-order for a newer model
        9.74, false, 0.24, true, false, 1.02, 7.02, 'ELEC-REF-174', 38, 19,
        '65f023632bc46470c104b75f', 14) -- Type 14 (LAPTOPS - *Tablet*)
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (347, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Apple iPad Air 11-inch (M2)</h1><p>Serious performance in a thin and light design. The redesigned iPad Air comes supercharged by the Apple M2 chip, features a stunning Liquid Retina display, and works with Apple Pencil Pro and Magic Keyboard.</p><h2>Key Features:</h2><ul><li>Apple M2 chip</li><li>11-inch Liquid Retina display with True Tone</li><li>Landscape 12MP Ultra Wide front camera with Center Stage</li><li>12MP Wide back camera</li><li>USB-C connector</li><li>Support for Apple Pencil Pro and Magic Keyboard</li><li>iPadOS</li></ul><h2>Specifications:</h2><ul><li>Chip: Apple M2 (8-core CPU, 10-core GPU)</li><li>Display: 11-inch Liquid Retina (2360x1640)</li><li>Storage: 128GB, 256GB, 512GB, 1TB</li><li>Connectivity: Wi-Fi 6E, Optional 5G Cellular</li></ul>',
        'Apple iPad Air 11-inch (M2)', 'Apple iPad Air 11" with M2 Chip',
        'The new iPad Air features the powerful M2 chip, a beautiful 11-inch Liquid Retina display, and support for Apple Pencil Pro in a portable design.',
        'ipad air, m2 chip, apple, tablet, ipados, liquid retina, apple pencil pro',
        'Apple iPad Air (M2) | Light. Bright. Full of Might.',
        'M2 Chip, 11-inch Display', 'apple-ipad-air-11-m2', 'en', 174)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (348, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Apple iPad Air 11 pouces (M2)</h1><p>Des performances de haut vol dans un design fin et léger. L’iPad Air repensé est suralimenté par la puce Apple M2, est doté d’un superbe écran Liquid Retina et fonctionne avec l’Apple Pencil Pro et le Magic Keyboard.</p><h2>Caractéristiques Clés :</h2><ul><li>Puce Apple M2</li><li>Écran Liquid Retina de 11 pouces avec True Tone</li><li>Caméra avant ultra grand-angle 12 Mpx en mode paysage avec Cadre centré</li><li>Caméra arrière grand-angle 12 Mpx</li><li>Connecteur USB-C</li><li>Prise en charge de l’Apple Pencil Pro et du Magic Keyboard</li><li>iPadOS</li></ul><h2>Spécifications :</h2><ul><li>Puce : Apple M2 (CPU 8 cœurs, GPU 10 cœurs)</li><li>Écran : Liquid Retina 11 pouces (2360x1640)</li><li>Stockage : 128 Go, 256 Go, 512 Go, 1 To</li><li>Connectivité : Wi‑Fi 6E, Cellulaire 5G en option</li></ul>',
        'Apple iPad Air 11 pouces (M2)', 'Apple iPad Air 11" avec Puce M2',
        'Le nouvel iPad Air est doté de la puissante puce M2, d’un superbe écran Liquid Retina de 11 pouces et prend en charge l’Apple Pencil Pro dans un design portable.',
        'ipad air, puce m2, apple, tablette, ipados, liquid retina, apple pencil pro',
        'Apple iPad Air (M2) | Léger. Brillant. Plein de talents.',
        'Puce M2, Écran 11 pouces', 'apple-ipad-air-11-m2', 'fr', 174)
on conflict (description_id) do nothing;

-- Product 175: Sony Bravia 7 QLED TV (65-inch) (Man: 21, Type: 16)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (175, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000', true, '2024-05-01 00:00:00.000000',
        true, -- Assuming pre-order
        32.8, false, 2.3, true, false, 50.3, 57.0, 'ELEC-REF-175', 39, 21,
        '65f023632bc46470c104b75f', 16) -- Type 16 (TELEVISIONS)
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (349, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Sony BRAVIA 7 65-inch QLED 4K HDR Google TV (K-65XR70)</h1><p>Cinema is coming home. Experience exceptional brightness and color with Mini LED and XR Backlight Master Drive™, powered by the XR Processor™.</p><h2>Key Features:</h2><ul><li>Mini LED with XR Backlight Master Drive™</li><li>XR Processor™ & XR Triluminos Pro™</li><li>X-Anti Reflection & X-Wide Angle™</li><li>Acoustic Multi-Audio+™</li><li>Google TV with Hands-free Voice Search</li><li>Perfect for PlayStation® 5</li></ul><h2>Specifications:</h2><ul><li>Screen Size: 65 inches</li><li>Resolution: 4K Ultra HD (3840 x 2160)</li><li>Display Type: Mini LED (QLED)</li><li>Processor: XR Processor™</li><li>Smart TV Platform: Google TV</li></ul>',
        'Sony Bravia 7 65-inch QLED TV', 'Sony BRAVIA 7 (XR70) 65" QLED 4K HDR Google TV',
        'The Sony Bravia 7 QLED TV delivers cinematic brightness and contrast with Mini LED technology and the intelligent XR Processor.',
        'sony bravia 7, xr70, qled tv, mini led, 65 inch tv, 4k tv, google tv, xr processor',
        'Sony Bravia 7 QLED TV | Cinematic Brightness',
        'Mini LED, XR Processor', 'sony-bravia-7-65-inch-qled-tv', 'en', 175)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (350, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Téléviseur Google TV Sony BRAVIA 7 65 pouces QLED 4K HDR (K-65XR70)</h1><p>Le cinéma s''invite chez vous. Profitez d''une luminosité et de couleurs exceptionnelles grâce au Mini LED et au XR Backlight Master Drive™, optimisés par le XR Processor™.</p><h2>Caractéristiques Clés :</h2><ul><li>Mini LED avec XR Backlight Master Drive™</li><li>XR Processor™ & XR Triluminos Pro™</li><li>X-Anti Reflection & X-Wide Angle™</li><li>Acoustic Multi-Audio+™</li><li>Google TV avec Recherche Vocale Mains Libres</li><li>Parfait pour PlayStation® 5</li></ul><h2>Spécifications :</h2><ul><li>Taille de l''écran : 65 pouces</li><li>Résolution : 4K Ultra HD (3840 x 2160)</li><li>Type d''affichage : Mini LED (QLED)</li><li>Processeur : XR Processor™</li><li>Plateforme Smart TV : Google TV</li></ul>',
        'Téléviseur Sony Bravia 7 65 pouces QLED', 'Téléviseur Google TV Sony BRAVIA 7 (XR70) 65" QLED 4K HDR',
        'Le téléviseur Sony Bravia 7 QLED offre une luminosité et un contraste cinématographiques grâce à la technologie Mini LED et à l''intelligent XR Processor.',
        'sony bravia 7, xr70, tv qled, mini led, tv 65 pouces, tv 4k, google tv, xr processor',
        'Téléviseur Sony Bravia 7 QLED | Luminosité Cinématographique',
        'Mini LED, XR Processor', 'sony-bravia-7-65-pouces-qled-tv', 'fr', 175)
on conflict (description_id) do nothing;

-- Product 176: Dell Latitude 7440 Laptop (Man: 23, Type: 14)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (176, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        0.72, false, 8.27, true, false, 3.0, 12.32, 'ELEC-REF-176', 40, 23,
        '65f023632bc46470c104b75f', 14) -- Type 14 (LAPTOPS)
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (351, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Dell Latitude 7440 Laptop</h1><p>Work smarter with the premium Latitude 7440. This 14-inch laptop offers intelligent performance, enhanced security features, and a sustainable design for modern professionals.</p><h2>Key Features:</h2><ul><li>14-inch FHD+ or QHD+ Display</li><li>13th Gen Intel® Core™ processors (vPro® options)</li><li>Intelligent Audio and Privacy features</li><li>ComfortView Plus low blue light</li><li>Lightweight and premium chassis</li><li>Windows 11 Pro</li></ul><h2>Specifications:</h2><ul><li>Display: 14" FHD+ (1920x1200) or QHD+ (2560x1600)</li><li>Processor: 13th Gen Intel® Core™ i5/i7 (vPro®)</li><li>RAM: 16GB / 32GB LPDDR5</li><li>Storage: 256GB / 512GB / 1TB NVMe SSD</li><li>Operating System: Windows 11 Pro</li></ul>',
        'Dell Latitude 7440 Laptop', 'Dell Latitude 7440 - Premium Business Laptop',
        'The Dell Latitude 7440 is a 14-inch business laptop designed for productivity and security, featuring 13th Gen Intel Core processors and intelligent features.',
        'dell latitude 7440, business laptop, windows 11 pro, intel core, vpro, 14 inch laptop',
        'Dell Latitude 7440 | Work Smarter',
        '13th Gen Intel vPro', 'dell-latitude-7440-laptop', 'en', 176)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (352, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Ordinateur Portable Dell Latitude 7440</h1><p>Travaillez plus intelligemment avec le Latitude 7440 haut de gamme. Cet ordinateur portable de 14 pouces offre des performances intelligentes, des fonctions de sécurité améliorées et une conception durable pour les professionnels modernes.</p><h2>Caractéristiques Clés :</h2><ul><li>Écran FHD+ ou QHD+ 14 pouces</li><li>Processeurs Intel® Core™ de 13e génération (options vPro®)</li><li>Fonctionnalités audio et de confidentialité intelligentes</li><li>ComfortView Plus à faible lumière bleue</li><li>Châssis léger et premium</li><li>Windows 11 Professionnel</li></ul><h2>Spécifications :</h2><ul><li>Écran : 14" FHD+ (1920x1200) ou QHD+ (2560x1600)</li><li>Processeur : Intel® Core™ i5/i7 de 13e génération (vPro®)</li><li>RAM : 16 Go / 32 Go LPDDR5</li><li>Stockage : 256 Go / 512 Go / 1 To SSD NVMe</li><li>Système d''exploitation : Windows 11 Professionnel</li></ul>',
        'Ordinateur Portable Dell Latitude 7440',
        'Dell Latitude 7440 - Ordinateur Portable Professionnel Haut de Gamme',
        'Le Dell Latitude 7440 est un ordinateur portable professionnel de 14 pouces conçu pour la productivité et la sécurité, doté de processeurs Intel Core de 13e génération et de fonctionnalités intelligentes.',
        'dell latitude 7440, portable professionnel, windows 11 pro, intel core, vpro, portable 14 pouces',
        'Dell Latitude 7440 | Travaillez Plus Intelligemment',
        'Intel vPro 13e Gén', 'dell-latitude-7440-portable', 'fr', 176)
on conflict (description_id) do nothing;

-- Product 177: Samsung Galaxy Watch6 (Man: 20, Type: 13)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (177, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        1.57, false, 0.35, true, false, 0.06, 1.53, 'ELEC-REF-177', 41, 20,
        '65f023632bc46470c104b75f', 13) -- Type 13 (SMARTPHONES - *Smartwatch*)
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (353, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Samsung Galaxy Watch6</h1><p>Start your everyday wellness journey. Galaxy Watch6 features a larger screen, personalized heart rate zones, advanced sleep coaching, and seamless Galaxy connectivity.</p><h2>Key Features:</h2><ul><li>Larger Display with Slimmer Bezel</li><li>Advanced Sleep Coaching</li><li>Body Composition Analysis (BIA)</li><li>Personalized Heart Rate Zones</li><li>Track Workouts Automatically</li><li>Wear OS Powered by Samsung</li></ul><h2>Specifications:</h2><ul><li>Sizes: 40mm, 44mm</li><li>Display: Super AMOLED, Sapphire Crystal Glass</li><li>Processor: Exynos W930 Dual-Core 1.4GHz</li><li>RAM: 2GB</li><li>Storage: 16GB</li><li>Sensors: BioActive Sensor (ECG, BIA, Heart Rate), Temperature Sensor</li></ul>',
        'Samsung Galaxy Watch6', 'Samsung Galaxy Watch6 - Wellness Smartwatch',
        'Track your health and fitness with the Samsung Galaxy Watch6, featuring a larger display, sleep coaching, body composition analysis, and more.',
        'galaxy watch6, samsung, smartwatch, wear os, fitness tracker, health tracking, sleep coaching',
        'Samsung Galaxy Watch6 | Your Wellness Coach',
        'Larger Display, Sleep Coaching', 'samsung-galaxy-watch6', 'en', 177)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (354, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Samsung Galaxy Watch6</h1><p>Commencez votre parcours de bien-être quotidien. La Galaxy Watch6 est dotée d''un écran plus grand, de zones de fréquence cardiaque personnalisées, d''un coaching avancé du sommeil et d''une connectivité Galaxy transparente.</p><h2>Caractéristiques Clés :</h2><ul><li>Écran Plus Grand avec Lunette Plus Fine</li><li>Coaching Avancé du Sommeil</li><li>Analyse de la Composition Corporelle (BIA)</li><li>Zones de Fréquence Cardiaque Personnalisées</li><li>Suivi Automatique des Entraînements</li><li>Wear OS Powered by Samsung</li></ul><h2>Spécifications :</h2><ul><li>Tailles : 40 mm, 44 mm</li><li>Écran : Super AMOLED, Verre Saphir</li><li>Processeur : Exynos W930 Dual-Core 1.4GHz</li><li>RAM : 2 Go</li><li>Stockage : 16 Go</li><li>Capteurs : Capteur BioActive (ECG, BIA, Fréquence Cardiaque), Capteur de Température</li></ul>',
        'Samsung Galaxy Watch6', 'Samsung Galaxy Watch6 - Montre Connectée Bien-être',
        'Suivez votre santé et votre forme physique avec la Samsung Galaxy Watch6, dotée d''un écran plus grand, d''un coaching du sommeil, d''une analyse de la composition corporelle, et plus encore.',
        'galaxy watch6, samsung, smartwatch, wear os, suivi fitness, suivi santé, coaching sommeil',
        'Samsung Galaxy Watch6 | Votre Coach Bien-être',
        'Écran Plus Grand, Coaching Sommeil', 'samsung-galaxy-watch6', 'fr', 177)
on conflict (description_id) do nothing;

-- Product 178: Beats Fit Pro Earbuds (Man: 19, Type: 15)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (178, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        null, false, null, true, false, 0.012, null, 'ELEC-REF-178', 42, 19,
        '65f023632bc46470c104b75f', 15) -- Type 15 (HEADPHONES)
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (355, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Beats Fit Pro - True Wireless Noise Cancelling Earbuds</h1><p>Designed for comfort, engineered for sound. Beats Fit Pro feature comfortable, secure-fit wingtips, Active Noise Cancelling, Spatial Audio, and the Apple H1 chip.</p><h2>Key Features:</h2><ul><li>Flexible, secure-fit wingtips</li><li>Active Noise Cancelling (ANC) and Transparency mode</li><li>Spatial Audio with dynamic head tracking</li><li>Apple H1 chip for seamless connectivity</li><li>Sweat and water resistant (IPX4-rated)</li><li>Up to 24 hours of combined battery life</li></ul><h2>Specifications:</h2><ul><li>Chip: Apple H1</li><li>Connectivity: Class 1 Bluetooth®</li><li>Battery Life: Up to 6 hours (ANC on), Up to 24 hours (with case)</li><li>Water Resistance: IPX4 (Earbuds)</li></ul>',
        'Beats Fit Pro Earbuds', 'Beats Fit Pro True Wireless Noise Cancelling Earbuds',
        'Beats Fit Pro are secure-fitting wireless earbuds with Active Noise Cancelling, Spatial Audio, and the Apple H1 chip for workouts and everyday listening.',
        'beats fit pro, earbuds, true wireless, noise cancelling, apple h1, spatial audio, workout earbuds',
        'Beats Fit Pro | Secure Fit, Premium Sound',
        'Secure-fit Wingtips, ANC', 'beats-fit-pro-earbuds', 'en', 178)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (356, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Beats Fit Pro - Écouteurs True Wireless à Réduction de Bruit</h1><p>Conçus pour le confort, pensés pour le son. Les Beats Fit Pro sont dotés d’ailettes de maintien confortables et sûres, de la Réduction active du bruit, de l’Audio spatial et de la puce Apple H1.</p><h2>Caractéristiques Clés :</h2><ul><li>Ailettes de maintien flexibles et sûres</li><li>Réduction active du bruit (ANC) et mode Transparence</li><li>Audio spatial avec suivi dynamique de la tête</li><li>Puce Apple H1 pour une connectivité transparente</li><li>Résistance à la transpiration et à l’eau (indice IPX4)</li><li>Jusqu’à 24 heures d’autonomie combinée</li></ul><h2>Spécifications :</h2><ul><li>Puce : Apple H1</li><li>Connectivité : Bluetooth® Classe 1</li><li>Autonomie : Jusqu’à 6 heures (ANC activé), Jusqu’à 24 heures (avec étui)</li><li>Résistance à l’eau : IPX4 (Écouteurs)</li></ul>',
        'Écouteurs Beats Fit Pro', 'Écouteurs True Wireless à Réduction de Bruit Beats Fit Pro',
        'Les Beats Fit Pro sont des écouteurs sans fil à maintien sûr avec Réduction active du bruit, Audio spatial et puce Apple H1 pour les entraînements et l''écoute quotidienne.',
        'beats fit pro, écouteurs, true wireless, réduction de bruit, apple h1, audio spatial, écouteurs sport',
        'Beats Fit Pro | Maintien Sûr, Son Premium',
        'Ailettes Maintien Sûr, ANC', 'beats-fit-pro-ecouteurs', 'fr', 178)
on conflict (description_id) do nothing;

-- Product 179: HP EliteBook 840 G10 Laptop (Man: 24, Type: 14)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (179, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        0.76, false, 8.43, true, false, 3.04, 12.42, 'ELEC-REF-179', 43, 24,
        '65f023632bc46470c104b75f', 14) -- Type 14 (LAPTOPS)
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (357, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>HP EliteBook 840 G10 Laptop</h1><p>Empower your hybrid work life with the HP EliteBook 840. This enterprise business laptop offers premium performance, collaboration features, and robust security in a refined design.</p><h2>Key Features:</h2><ul><li>14-inch WUXGA Display (16:10 aspect ratio)</li><li>13th Gen Intel® Core™ processors (vPro® options)</li><li>HP Presence collaboration tools (5MP IR Camera, AI Noise Reduction)</li><li>HP Wolf Security for Business</li><li>Lightweight and durable design</li><li>Windows 11 Pro</li></ul><h2>Specifications:</h2><ul><li>Display: 14" WUXGA (1920x1200) IPS</li><li>Processor: 13th Gen Intel® Core™ i5/i7 (vPro®)</li><li>RAM: 16GB / 32GB DDR5</li><li>Storage: 512GB / 1TB PCIe® NVMe™ SSD</li><li>Operating System: Windows 11 Pro</li></ul>',
        'HP EliteBook 840 G10 Laptop', 'HP EliteBook 840 G10 - Enterprise Business Laptop',
        'The HP EliteBook 840 G10 is a secure and powerful business laptop designed for hybrid work, featuring 13th Gen Intel Core processors and HP Presence.',
        'hp elitebook 840 g10, business laptop, windows 11 pro, intel core, vpro, hp wolf security',
        'HP EliteBook 840 G10 | Secure Hybrid Work',
        'HP Wolf Security, HP Presence', 'hp-elitebook-840-g10-laptop', 'en', 179)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (358, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Ordinateur Portable HP EliteBook 840 G10</h1><p>Dynamisez votre vie professionnelle hybride avec le HP EliteBook 840. Cet ordinateur portable professionnel d''entreprise offre des performances haut de gamme, des fonctionnalités de collaboration et une sécurité robuste dans un design raffiné.</p><h2>Caractéristiques Clés :</h2><ul><li>Écran WUXGA 14 pouces (format 16:10)</li><li>Processeurs Intel® Core™ de 13e génération (options vPro®)</li><li>Outils de collaboration HP Presence (Caméra IR 5MP, Réduction du bruit IA)</li><li>HP Wolf Security for Business</li><li>Design léger et durable</li><li>Windows 11 Professionnel</li></ul><h2>Spécifications :</h2><ul><li>Écran : 14" WUXGA (1920x1200) IPS</li><li>Processeur : Intel® Core™ i5/i7 de 13e génération (vPro®)</li><li>RAM : 16 Go / 32 Go DDR5</li><li>Stockage : 512 Go / 1 To SSD PCIe® NVMe™</li><li>Système d''exploitation : Windows 11 Professionnel</li></ul>',
        'Ordinateur Portable HP EliteBook 840 G10',
        'HP EliteBook 840 G10 - Ordinateur Portable Professionnel d''Entreprise',
        'Le HP EliteBook 840 G10 est un ordinateur portable professionnel sécurisé et puissant conçu pour le travail hybride, doté de processeurs Intel Core de 13e génération et de HP Presence.',
        'hp elitebook 840 g10, portable professionnel, windows 11 pro, intel core, vpro, hp wolf security',
        'HP EliteBook 840 G10 | Travail Hybride Sécurisé',
        'HP Wolf Security, HP Presence', 'hp-elitebook-840-g10-portable', 'fr', 179)
on conflict (description_id) do nothing;

-- Product 180: LG S90QY Soundbar (Man: 22, Type: 16)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (180, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        2.5, false, 4.7, true, false, 9.9, 39.4, 'ELEC-REF-180', 44, 22,
        '65f023632bc46470c104b75f', 16) -- Type 16 (TELEVISIONS - *Soundbar*)
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (359, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>LG S90QY 5.1.3 ch High Res Audio Sound Bar with Dolby Atmos</h1><p>Elevate your home cinema experience with the LG S90QY soundbar. Featuring Dolby Atmos, DTS:X, and Meridian Horizon technology for immersive, high-resolution audio.</p><h2>Key Features:</h2><ul><li>5.1.3 Channel Sound with Up-firing Center Speaker</li><li>Dolby Atmos & DTS:X</li><li>Meridian Horizon Audio Technology</li><li>High Resolution Audio (24bit/96kHz)</li><li>AI Room Calibration & AI Sound Pro</li><li>Works with Google Assistant & Alexa</li></ul><h2>Specifications:</h2><ul><li>Channels: 5.1.3ch</li><li>Total Power: 570W</li><li>Sound Technologies: Dolby Atmos, DTS:X, Meridian Horizon, Hi-Res Audio</li><li>Connectivity: HDMI eARC/ARC, Optical, USB, Bluetooth, Wi-Fi</li></ul>',
        'LG S90QY Soundbar', 'LG S90QY 5.1.3 ch Dolby Atmos Soundbar',
        'The LG S90QY soundbar delivers powerful, immersive 5.1.3ch sound with Dolby Atmos, Meridian technology, and AI calibration.',
        'lg s90qy, soundbar, dolby atmos, dts:x, meridian audio, hi-res audio, home theater',
        'LG S90QY Soundbar | Immersive Audio Powerhouse',
        '5.1.3ch Dolby Atmos, Meridian', 'lg-s90qy-soundbar', 'en', 180)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (360, '2024-04-02 18:00:00.000000', '2024-04-02 18:00:00.000000',
        '<h1>Barre de Son LG S90QY 5.1.3 ch Audio Haute Résolution avec Dolby Atmos</h1><p>Améliorez votre expérience home cinéma avec la barre de son LG S90QY. Dotée de Dolby Atmos, DTS:X et de la technologie Meridian Horizon pour un son immersif haute résolution.</p><h2>Caractéristiques Clés :</h2><ul><li>Son 5.1.3 canaux avec Haut-parleur Central Orienté vers le Haut</li><li>Dolby Atmos & DTS:X</li><li>Technologie Audio Meridian Horizon</li><li>Audio Haute Résolution (24bit/96kHz)</li><li>Calibration IA de la pièce & AI Sound Pro</li><li>Fonctionne avec Google Assistant & Alexa</li></ul><h2>Spécifications :</h2><ul><li>Canaux : 5.1.3ch</li><li>Puissance totale : 570W</li><li>Technologies sonores : Dolby Atmos, DTS:X, Meridian Horizon, Hi-Res Audio</li><li>Connectivité : HDMI eARC/ARC, Optique, USB, Bluetooth, Wi-Fi</li></ul>',
        'Barre de Son LG S90QY', 'Barre de Son LG S90QY 5.1.3 ch Dolby Atmos',
        'La barre de son LG S90QY offre un son puissant et immersif 5.1.3ch avec Dolby Atmos, la technologie Meridian et la calibration IA.',
        'lg s90qy, barre de son, dolby atmos, dts:x, audio meridian, audio hi-res, home cinéma',
        'Barre de Son LG S90QY | Puissance Audio Immersive',
        'Dolby Atmos 5.1.3ch, Meridian', 'lg-s90qy-barre-de-son', 'fr', 180)
on conflict (description_id) do nothing;