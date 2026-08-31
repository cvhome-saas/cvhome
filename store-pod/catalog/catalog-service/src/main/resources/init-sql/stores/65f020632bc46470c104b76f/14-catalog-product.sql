/*
Generated SQL inserts similar to the provided template
where languages=['fr','en'] and store_id='65f020632bc46470c104b76f'
and manufacturer_id in (7,8,9,10,11,12) and product_type_id in (5,6,7,8)
start product id from 46
start product_description from 91
generate 15 products
make sure to replace all ids with real number in sequence
use html format for description generator so it contain valid html tags describe the product and its features and specification and more
domain for this store is beauté
Timestamps replaced with NOW()
*/

-- Product 46: YSL Touche Éclat Illuminating Pen (Manuf: 7, Type: 6)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (46, NOW(), NOW(), true, NOW(), false, 12.5, -- Approx height in cm
        false, null, true, false, 0.03, 1.5, -- Approx weight in kg, width in cm
        'REF-YSL-MAKE-TEIPEN46', 46, 7,
        '65f020632bc46470c104b76f', 6)
on conflict (product_id) do nothing;

-- French Description (ID: 91)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (91, NOW(), NOW(),
        '<div>
           <h2>Stylo Illuminateur Touche Éclat Yves Saint Laurent</h2>
           <p>L''outil de beauté iconique pour un teint lumineux et unifié. Efface les signes de fatigue et illumine le teint en un clic.</p>
           <h3>Bénéfices :</h3>
           <ul>
             <li><strong>Éclat Instantané :</strong> Capture la lumière pour illuminer les zones d''ombre.</li>
             <li><strong>Anti-Fatigue :</strong> Atténue l''apparence des cernes et des signes de fatigue.</li>
             <li><strong>Effet Lissant :</strong> Lisse ridules et imperfections sans marquer.</li>
             <li><strong>Format Nomade :</strong> Stylo-pinceau pratique pour des retouches à tout moment.</li>
           </ul>
           <h3>Conseils d''utilisation :</h3>
           <p>Cliquez pour imbiber le pinceau. Appliquez par touches sur les zones à illuminer ou corriger (cernes, ailes du nez, ridules) puis estompez du bout des doigts.</p>
           <h3>Spécifications :</h3>
           <ul>
             <li><strong>Gamme :</strong> Touche Éclat</li>
             <li><strong>Type :</strong> Stylo Illuminateur / Correcteur</li>
             <li><strong>Teinte :</strong> Plusieurs teintes disponibles (Exemple: 2 Ivoire Lumière)</li>
             <li><strong>Contenance :</strong> 2.5ml</li>
           </ul>
         </div>',
        'YSL Touche Éclat Stylo Illuminateur', 'Yves Saint Laurent Touche Éclat Stylo Illuminateur Éclat & Anti-Cernes',
        'Illuminez votre teint et effacez la fatigue avec le stylo iconique Touche Éclat d''Yves Saint Laurent.',
        'YSL, Touche Éclat, stylo illuminateur, anti-cernes, éclat, maquillage teint',
        'YSL Touche Éclat Stylo Illuminateur | Éclat Instantané', 'Illumine et corrige',
        'ysl-touche-eclat-illuminating-pen', 'fr', 46)
on conflict (description_id) do nothing;

-- English Description (ID: 92)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (92, NOW(), NOW(),
        '<div>
           <h2>Yves Saint Laurent Touche Éclat Illuminating Pen</h2>
           <p>The iconic beauty tool for a luminous and unified complexion. Erases signs of fatigue and brightens the complexion in one click.</p>
           <h3>Benefits:</h3>
           <ul>
             <li><strong>Instant Radiance:</strong> Captures light to illuminate shadowy areas.</li>
             <li><strong>Anti-Fatigue:</strong> Diminishes the appearance of dark circles and signs of fatigue.</li>
             <li><strong>Smoothing Effect:</strong> Smooths fine lines and imperfections without creasing.</li>
             <li><strong>Portable Format:</strong> Convenient brush pen for touch-ups anytime.</li>
           </ul>
           <h3>How to Use:</h3>
           <p>Click to dispense product onto the brush. Apply in touches to areas needing illumination or correction (dark circles, sides of nose, fine lines) then blend with fingertips.</p>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Range:</strong> Touche Éclat</li>
             <li><strong>Type:</strong> Illuminating Pen / Concealer</li>
             <li><strong>Shade:</strong> Several shades available (Example: 2 Luminous Ivory)</li>
             <li><strong>Volume:</strong> 2.5ml</li>
           </ul>
         </div>',
        'YSL Touche Éclat Illuminating Pen', 'Yves Saint Laurent Touche Éclat Radiance & Dark Circle Illuminating Pen',
        'Brighten your complexion and erase fatigue with the iconic Yves Saint Laurent Touche Éclat pen.',
        'YSL, Touche Éclat, illuminating pen, concealer, radiance, face makeup',
        'YSL Touche Éclat Illuminating Pen | Instant Radiance', 'Illuminates and corrects',
        'ysl-touche-eclat-illuminating-pen-en', 'en', 46)
on conflict (description_id) do nothing;

-- Product 47: Guerlain Abeille Royale Advanced Youth Watery Oil (Manuf: 8, Type: 5)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (47, NOW(), NOW(), true, NOW(), false, 12.0,
        false, null, true, false, 0.2, 5.0,
        'REF-GUER-SKIN-ARWOIL47', 47, 8,
        '65f020632bc46470c104b76f', 5)
on conflict (product_id) do nothing;

-- French Description (ID: 93)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (93, NOW(), NOW(),
        '<div>
           <h2>Huile-en-Eau Jeunesse Abeille Royale Guerlain</h2>
           <p>L''Huile-en-Eau Jeunesse Abeille Royale offre à la peau toute la puissance des miels et de la gelée royale exclusive Guerlain. La peau est repulpée, plus lisse et lumineuse.</p>
           <h3>Technologie Blackbee Repair :</h3>
           <ul>
             <li><strong>Concentré de Miels :</strong> Miel de l''Abeille Noire d''Ouessant, Miel de Corse, Miel de Trèfle de Nouvelle-Zélande.</li>
             <li><strong>Gelée Royale Exclusive :</strong> Hautement concentrée en nutriments.</li>
             <li><strong>Micro-billes Actives :</strong> Libèrent leurs actifs au dernier moment pour une efficacité préservée.</li>
             <li><strong>Texture Surprenante :</strong> La richesse d''une huile, la légèreté d''une lotion, la puissance d''un sérum.</li>
           </ul>
           <h3>Résultats :</h3>
           <p>La peau se repulpe spectaculairement. Elle est visiblement plus lisse, éclatante de jeunesse. Réparation 9 fois plus rapide*.</p>
           <h3>Spécifications :</h3>
           <ul>
             <li><strong>Gamme :</strong> Abeille Royale</li>
             <li><strong>Contenance :</strong> 50ml (Exemple)</li>
             <li><strong>Type de peau :</strong> Tous types de peaux.</li>
           </ul>
           <p><small>*Test instrumental, 20 volontaires, 2 applications par jour, après 3 jours.</small></p>
         </div>',
        'Guerlain Abeille Royale Huile-en-Eau Jeunesse',
        'Guerlain Abeille Royale Huile-en-Eau Jeunesse Réparation Avancée',
        'Réparez et repulpez votre peau avec l''Huile-en-Eau Jeunesse Abeille Royale de Guerlain, concentrée en miels et gelée royale.',
        'Guerlain, Abeille Royale, huile-en-eau, sérum, anti-âge, réparation, miel, gelée royale',
        'Guerlain Abeille Royale Huile-en-Eau Jeunesse | Réparation Avancée', 'Réparation 9x plus rapide',
        'guerlain-abeille-royale-advanced-youth-watery-oil', 'fr', 47)
on conflict (description_id) do nothing;

-- English Description (ID: 94)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (94, NOW(), NOW(),
        '<div>
           <h2>Guerlain Abeille Royale Advanced Youth Watery Oil</h2>
           <p>Abeille Royale Advanced Youth Watery Oil offers the skin all the power of Guerlain-exclusive honeys and royal jelly. Skin is plumped, smoother and more luminous.</p>
           <h3>Blackbee Repair Technology:</h3>
           <ul>
             <li><strong>Honey Concentrate:</strong> Ouessant Black Bee Honey, Corsican Honey, New Zealand Clover Honey.</li>
             <li><strong>Exclusive Royal Jelly:</strong> Highly concentrated in nutrients.</li>
             <li><strong>Active Micro-beads:</strong> Release their active ingredients at the last moment for preserved efficacy.</li>
             <li><strong>Surprising Texture:</strong> The richness of an oil, the lightness of a lotion, the power of a serum.</li>
           </ul>
           <h3>Results:</h3>
           <p>Skin plumps up spectacularly. It is visibly smoother, glowing with youthfulness. 9 times faster repair*.</p>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Range:</strong> Abeille Royale</li>
             <li><strong>Volume:</strong> 50ml (Example)</li>
             <li><strong>Skin Type:</strong> All skin types.</li>
           </ul>
           <p><small>*Instrumental test, 20 volunteers, 2 applications per day, after 3 days.</small></p>
         </div>',
        'Guerlain Abeille Royale Advanced Youth Watery Oil',
        'Guerlain Abeille Royale Advanced Youth Watery Oil Advanced Repair',
        'Repair and plump your skin with Guerlain''s Abeille Royale Advanced Youth Watery Oil, concentrated in honeys and royal jelly.',
        'Guerlain, Abeille Royale, watery oil, serum, anti-aging, repair, honey, royal jelly',
        'Guerlain Abeille Royale Advanced Youth Watery Oil | Advanced Repair', '9x faster repair',
        'guerlain-abeille-royale-advanced-youth-watery-oil-en', 'en', 47)
on conflict (description_id) do nothing;

-- Product 48: Shiseido Ultimune Power Infusing Concentrate (Manuf: 9, Type: 5)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (48, NOW(), NOW(), true, NOW(), false, 16.0,
        false, null, true, false, 0.3, 5.5,
        'REF-SHIS-SKIN-ULTIMUNE48', 48, 9,
        '65f020632bc46470c104b76f', 5)
on conflict (product_id) do nothing;

-- French Description (ID: 95)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (95, NOW(), NOW(),
        '<div>
           <h2>Concentré Activateur Énergisant Ultimune Shiseido</h2>
           <p>Le sérum anti-âge N°1 de Shiseido. Renforce les défenses internes de la peau et prévient les dommages liés au vieillissement. Pour une peau plus lisse, plus ferme, plus résistante.</p>
           <h3>Technologie ImuGenerationRED™ :</h3>
           <ul>
             <li><strong>Double Défense Anti-Âge :</strong> Stimule la fonction des cellules de Langerhans et renforce le réseau de collagène.</li>
             <li><strong>Cocktail Botanique :</strong> Extraits de Reishi, d''Iris, de Lotus et de Ginko Biloba.</li>
             <li><strong>Texture Fraîche :</strong> Pénètre rapidement et laisse la peau douce et hydratée.</li>
             <li><strong>Parfum Relaxant :</strong> Notes florales vertes ImuCalm Compound™.</li>
           </ul>
           <h3>Résultats :</h3>
           <p>Dès 3 jours, la peau est visiblement plus éclatante*. En 4 semaines, la peau est plus ferme et plus résiliente**.</p>
           <h3>Spécifications :</h3>
           <ul>
             <li><strong>Gamme :</strong> Ultimune</li>
             <li><strong>Contenance :</strong> 50ml (Exemple)</li>
             <li><strong>Type de peau :</strong> Tous types de peaux.</li>
           </ul>
           <p><small>*Test consommateurs sur 103 femmes.<br/>**Test clinique sur 32 femmes.</small></p>
         </div>',
        'Shiseido Ultimune Concentré Activateur Énergisant',
        'Shiseido Ultimune Power Infusing Concentrate Sérum Anti-Âge',
        'Renforcez les défenses de votre peau avec le sérum Ultimune de Shiseido pour une peau plus forte et éclatante de jeunesse.',
        'Shiseido, Ultimune, sérum, anti-âge, défense peau, activateur énergisant, soin visage',
        'Shiseido Ultimune Sérum | Défense Anti-Âge', 'Technologie ImuGenerationRED™',
        'shiseido-ultimune-power-infusing-concentrate', 'fr', 48)
on conflict (description_id) do nothing;

-- English Description (ID: 96)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (96, NOW(), NOW(),
        '<div>
           <h2>Shiseido Ultimune Power Infusing Concentrate</h2>
           <p>Shiseido''s #1 anti-aging serum. Strengthens the skin''s internal defenses and prevents damage related to aging. For smoother, firmer, more resilient skin.</p>
           <h3>ImuGenerationRED™ Technology:</h3>
           <ul>
             <li><strong>Double Anti-Aging Defense:</strong> Boosts the function of Langerhans cells and strengthens the collagen network.</li>
             <li><strong>Botanical Cocktail:</strong> Reishi Mushroom, Iris Root, Lotus, and Ginko Biloba Extracts.</li>
             <li><strong>Fresh Texture:</strong> Penetrates quickly, leaving skin soft and hydrated.</li>
             <li><strong>Relaxing Fragrance:</strong> ImuCalm Compound™ green floral notes.</li>
           </ul>
           <h3>Results:</h3>
           <p>From 3 days, skin is visibly more radiant*. In 4 weeks, skin is firmer and more resilient**.</p>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Range:</strong> Ultimune</li>
             <li><strong>Volume:</strong> 50ml (Example)</li>
             <li><strong>Skin Type:</strong> All skin types.</li>
           </ul>
           <p><small>*Consumer test on 103 women.<br/>**Clinical test on 32 women.</small></p>
         </div>',
        'Shiseido Ultimune Power Infusing Concentrate', 'Shiseido Ultimune Power Infusing Concentrate Anti-Aging Serum',
        'Strengthen your skin''s defenses with Shiseido''s Ultimune serum for stronger, youthfully radiant skin.',
        'Shiseido, Ultimune, serum, anti-aging, skin defense, power infusing concentrate, face care',
        'Shiseido Ultimune Serum | Anti-Aging Defense', 'ImuGenerationRED™ Technology',
        'shiseido-ultimune-power-infusing-concentrate-en', 'en', 48)
on conflict (description_id) do nothing;

-- Product 49: NARS Radiant Creamy Concealer (Manuf: 10, Type: 6)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (49, NOW(), NOW(), true, NOW(), false, 11.0,
        false, null, true, false, 0.04, 1.8,
        'REF-NARS-MAKE-RCCONC49', 49, 10,
        '65f020632bc46470c104b76f', 6)
on conflict (product_id) do nothing;

-- French Description (ID: 97)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (97, NOW(), NOW(),
        '<div>
           <h2>Correcteur Radiant Creamy Concealer NARS</h2>
           <p>L''anti-cernes n°1 aux États-Unis*. Un correcteur multi-actions qui illumine, corrige et perfectionne le teint avec une couvrance modulable et un fini lumineux naturel.</p>
           <h3>Bénéfices :</h3>
           <ul>
             <li><strong>Couvrance Modulable :</strong> De moyenne à haute, camoufle imperfections et cernes.</li>
             <li><strong>Fini Lumineux :</strong> Illumine le teint sans effet brillant.</li>
             <li><strong>Longue Tenue & Hydratant :</strong> Ne file pas dans les plis, confortable toute la journée.</li>
             <li><strong>Multi-Usage :</strong> Idéal pour corriger, illuminer et sculpter.</li>
           </ul>
           <h3>Technologie :</h3>
           <p>Technologie de diffusion de la lumière pour un effet floutant. Enrichi en extraits botaniques hydratants.</p>
           <h3>Spécifications :</h3>
           <ul>
             <li><strong>Gamme :</strong> Radiant Creamy Concealer</li>
             <li><strong>Contenance :</strong> 6ml</li>
             <li><strong>Teinte :</strong> Large gamme (Exemple: Vanilla - Clair rosé)</li>
             <li><strong>Formule :</strong> Non comédogène, sans alcool, sans parfum.</li>
           </ul>
           <p><small>*Source : The NPD Group/U.S. Prestige Beauty Total Department/Specialty, Ventes Maquillage, Janvier 2018 – Décembre 2020.</small></p>
         </div>',
        'NARS Radiant Creamy Concealer', 'NARS Radiant Creamy Concealer Anti-Cernes & Correcteur Éclat',
        'Corrigez, illuminez et perfectionnez votre teint avec le Radiant Creamy Concealer de NARS, l''anti-cernes culte.',
        'NARS, Radiant Creamy Concealer, anti-cernes, correcteur, maquillage teint, couvrance, éclat',
        'NARS Radiant Creamy Concealer | Anti-Cernes Éclat', 'Couvrance modulable, fini lumineux',
        'nars-radiant-creamy-concealer', 'fr', 49)
on conflict (description_id) do nothing;

-- English Description (ID: 98)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (98, NOW(), NOW(),
        '<div>
           <h2>NARS Radiant Creamy Concealer</h2>
           <p>The #1 concealer in the U.S.*. A multi-action concealer that brightens, corrects, and perfects the complexion with buildable coverage and a natural luminous finish.</p>
           <h3>Benefits:</h3>
           <ul>
             <li><strong>Buildable Coverage:</strong> Medium to full, conceals imperfections and dark circles.</li>
             <li><strong>Luminous Finish:</strong> Brightens the complexion without shine.</li>
             <li><strong>Long-Wearing & Hydrating:</strong> Crease-proof, comfortable all day.</li>
             <li><strong>Multi-Use:</strong> Ideal for correcting, highlighting, and contouring.</li>
           </ul>
           <h3>Technology:</h3>
           <p>Light-diffusing technology for a blurring effect. Enriched with hydrating botanical extracts.</p>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Range:</strong> Radiant Creamy Concealer</li>
             <li><strong>Volume:</strong> 6ml</li>
             <li><strong>Shade:</strong> Wide range (Example: Vanilla - Light with pink undertones)</li>
             <li><strong>Formula:</strong> Non-comedogenic, alcohol-free, fragrance-free.</li>
           </ul>
           <p><small>*Source: The NPD Group/U.S. Prestige Beauty Total Department/Specialty, Makeup Sales, January 2018 – December 2020.</small></p>
         </div>',
        'NARS Radiant Creamy Concealer', 'NARS Radiant Creamy Concealer Dark Circle & Radiance Corrector',
        'Correct, brighten, and perfect your complexion with NARS Radiant Creamy Concealer, the cult-favorite concealer.',
        'NARS, Radiant Creamy Concealer, concealer, corrector, face makeup, coverage, radiance',
        'NARS Radiant Creamy Concealer | Radiance Concealer', 'Buildable coverage, luminous finish',
        'nars-radiant-creamy-concealer-en', 'en', 49)
on conflict (description_id) do nothing;

-- Product 50: La Roche-Posay Anthelios UVMUNE 400 Fluid SPF50+ (Manuf: 11, Type: 5)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (50, NOW(), NOW(), true, NOW(), false, 14.0,
        false, null, true, false, 0.08, 4.5,
        'REF-LRP-SKIN-ANTHUV50', 50, 11,
        '65f020632bc46470c104b76f', 5)
on conflict (product_id) do nothing;

-- French Description (ID: 99)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (99, NOW(), NOW(),
        '<div>
           <h2>Fluide Invisible Anthelios UVMUNE 400 SPF50+ La Roche-Posay</h2>
           <p>La très haute protection UVA/UVB ultime, protégeant même contre les UVA ultra-longs*. Texture fluide invisible, ultra-résistante à l''eau, la transpiration et le sable.</p>
           <h3>Technologie Mexoryl 400 :</h3>
           <ul>
             <li><strong>Protection Ultime :</strong> Nouveau filtre UV exclusif protégeant contre les UVA ultra-longs [380-400nm], les plus insidieux.</li>
             <li><strong>Large Spectre :</strong> Protection efficace contre UVA, UVB et aide à prévenir les dommages induits par les infrarouges et la pollution.</li>
             <li><strong>Texture Invisible :</strong> Fini non gras, sans traces blanches. Idéal pour peaux sensibles.</li>
             <li><strong>Haute Tolérance :</strong> Formulé pour minimiser les picotements oculaires. Testé sur peaux sensibles et réactives.</li>
           </ul>
           <h3>Conseils d''utilisation :</h3>
           <p>Appliquer généreusement juste avant l''exposition. Renouveler fréquemment, surtout après avoir nagé, transpiré ou s''être essuyé.</p>
           <h3>Spécifications :</h3>
           <ul>
             <li><strong>Gamme :</strong> Anthelios</li>
             <li><strong>Protection :</strong> SPF50+ / UVA PF 46</li>
             <li><strong>Contenance :</strong> 50ml</li>
             <li><strong>Formule :</strong> Sans parfum, hypoallergénique.</li>
           </ul>
           <p><small>*[340-400nm] = UVA longs / [380-400nm] = fin du spectre que l’on appelle UVA ultra longs.</small></p>
         </div>',
        'La Roche-Posay Anthelios UVMUNE 400 Fluide Invisible SPF50+',
        'La Roche-Posay Anthelios UVMUNE 400 Fluide Solaire Invisible SPF50+',
        'Protection solaire ultime SPF50+ contre les UVA ultra-longs avec le fluide invisible Anthelios UVMUNE 400 de La Roche-Posay.',
        'La Roche-Posay, Anthelios, UVMUNE 400, protection solaire, SPF50+, UVA ultra-longs, peau sensible',
        'Protection Solaire La Roche-Posay Anthelios UVMUNE 400 SPF50+', 'Protège des UVA ultra-longs',
        'la-roche-posay-anthelios-uvmune-400-spf50', 'fr', 50)
on conflict (description_id) do nothing;

-- English Description (ID: 100)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (100, NOW(), NOW(),
        '<div>
           <h2>La Roche-Posay Anthelios UVMUNE 400 Invisible Fluid SPF50+</h2>
           <p>The ultimate very high UVA/UVB protection, even protecting against ultra-long UVA rays*. Invisible fluid texture, ultra-resistant to water, sweat, and sand.</p>
           <h3>Mexoryl 400 Technology:</h3>
           <ul>
             <li><strong>Ultimate Protection:</strong> New exclusive UV filter protecting against ultra-long UVA rays [380-400nm], the most insidious.</li>
             <li><strong>Broad Spectrum:</strong> Effective protection against UVA, UVB and helps prevent damage induced by infrared and pollution.</li>
             <li><strong>Invisible Texture:</strong> Non-greasy finish, no white marks. Ideal for sensitive skin.</li>
             <li><strong>High Tolerance:</strong> Formulated to minimize eye stinging. Tested on sensitive and reactive skin.</li>
           </ul>
           <h3>How to Use:</h3>
           <p>Apply generously just before exposure. Reapply frequently, especially after swimming, sweating, or toweling.</p>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Range:</strong> Anthelios</li>
             <li><strong>Protection:</strong> SPF50+ / UVA PF 46</li>
             <li><strong>Volume:</strong> 50ml</li>
             <li><strong>Formula:</strong> Fragrance-free, hypoallergenic.</li>
           </ul>
           <p><small>*[340-400nm] = long UVA / [380-400nm] = end of the spectrum called ultra-long UVA.</small></p>
         </div>',
        'La Roche-Posay Anthelios UVMUNE 400 Invisible Fluid SPF50+',
        'La Roche-Posay Anthelios UVMUNE 400 Invisible Sunscreen Fluid SPF50+',
        'Ultimate SPF50+ sun protection against ultra-long UVA rays with La Roche-Posay Anthelios UVMUNE 400 invisible fluid.',
        'La Roche-Posay, Anthelios, UVMUNE 400, sun protection, sunscreen, SPF50+, ultra-long UVA, sensitive skin',
        'Sunscreen La Roche-Posay Anthelios UVMUNE 400 SPF50+', 'Protects against ultra-long UVA',
        'la-roche-posay-anthelios-uvmune-400-spf50-en', 'en', 50)
on conflict (description_id) do nothing;

-- Product 51: Kérastase Genesis Bain Hydra-Fortifiant Shampoo (Manuf: 12, Type: 8)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (51, NOW(), NOW(), true, NOW(), false, 18.0,
        false, null, true, false, 0.3, 6.0, -- Approx for 250ml bottle
        'REF-KERA-HAIR-GENSHMP51', 51, 12,
        '65f020632bc46470c104b76f', 8)
on conflict (product_id) do nothing;

-- French Description (ID: 101)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (101, NOW(), NOW(),
        '<div>
           <h2>Shampooing Bain Hydra-Fortifiant Genesis Kérastase</h2>
           <p>Shampooing fortifiant anti-chute par casse pour cheveux fins et/ou à tendance grasse. Nettoie en douceur et renforce la fibre pour réduire la chute.</p>
           <h3>Bénéfices :</h3>
           <ul>
             <li><strong>Anti-Chute par Casse :</strong> Réduit la chute de cheveux due à la casse lors du brossage.</li>
             <li><strong>Renforce la Fibre :</strong> Formule enrichie en Cellules Natives d''Edelweiss et Racine de Gingembre.</li>
             <li><strong>Nettoie en Douceur :</strong> Élimine sébum et impuretés du cuir chevelu et des cheveux.</li>
             <li><strong>Hydratation Légère :</strong> N''alourdit pas les cheveux fins.</li>
           </ul>
           <h3>Conseils d''utilisation :</h3>
           <p>Répartir sur cheveux mouillés, masser, émulsionner puis rincer. Répéter si nécessaire.</p>
           <h3>Spécifications :</h3>
           <ul>
             <li><strong>Gamme :</strong> Genesis</li>
             <li><strong>Type de cheveux :</strong> Fins, Tendance Grasse, Fragilisés, Chute de cheveux</li>
             <li><strong>Contenance :</strong> 250ml</li>
           </ul>
         </div>',
        'Kérastase Genesis Bain Hydra-Fortifiant',
        'Kérastase Genesis Shampooing Fortifiant Anti-Chute pour Cheveux Fins',
        'Réduisez la chute de cheveux due à la casse avec le shampooing Genesis Bain Hydra-Fortifiant de Kérastase pour cheveux fins.',
        'Kérastase, Genesis, shampooing, anti-chute, cheveux fins, fortifiant, bain hydra-fortifiant',
        'Shampooing Kérastase Genesis Anti-Chute | Cheveux Fins', 'Réduit la chute par casse',
        'kerastase-genesis-bain-hydra-fortifiant-shampoo', 'fr', 51)
on conflict (description_id) do nothing;

-- English Description (ID: 102)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (102, NOW(), NOW(),
        '<div>
           <h2>Kérastase Genesis Bain Hydra-Fortifiant Shampoo</h2>
           <p>Anti hair-fall fortifying shampoo for weakened hair, prone to falling due to breakage. For fine and/or oily hair. Gently cleanses and reinforces fiber to reduce the risk of hair-fall.</p>
           <h3>Benefits:</h3>
           <ul>
             <li><strong>Anti Hair-Fall due to Breakage:</strong> Reduces hair fall from breakage during brushing.</li>
             <li><strong>Reinforces Fiber:</strong> Formula enriched with Edelweiss Native Cells and Ginger Root.</li>
             <li><strong>Gently Cleanses:</strong> Removes sebum and impurities from scalp and hair.</li>
             <li><strong>Lightweight Hydration:</strong> Does not weigh down fine hair.</li>
           </ul>
           <h3>How to Use:</h3>
           <p>Apply to wet hair, massage, emulsify, and rinse. Repeat if necessary.</p>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Range:</strong> Genesis</li>
             <li><strong>Hair Type:</strong> Fine, Oily Prone, Weakened, Hair Fall</li>
             <li><strong>Volume:</strong> 250ml</li>
           </ul>
         </div>',
        'Kérastase Genesis Bain Hydra-Fortifiant Shampoo',
        'Kérastase Genesis Anti Hair-Fall Fortifying Shampoo for Fine Hair',
        'Reduce hair fall due to breakage with Kérastase Genesis Bain Hydra-Fortifiant shampoo for fine hair.',
        'Kérastase, Genesis, shampoo, anti hair-fall, fine hair, fortifying, bain hydra-fortifiant',
        'Kérastase Genesis Anti Hair-Fall Shampoo | Fine Hair', 'Reduces hair-fall from breakage',
        'kerastase-genesis-bain-hydra-fortifiant-shampoo-en', 'en', 51)
on conflict (description_id) do nothing;

-- Product 52: YSL Black Opium Eau de Parfum (Manuf: 7, Type: 7)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (52, NOW(), NOW(), true, NOW(), false, 10.0,
        false, null, true, false, 0.3, 5.0, -- Approx for 50ml
        'REF-YSL-FRAG-BLACKOPIUM52', 52, 7,
        '65f020632bc46470c104b76f', 7)
on conflict (product_id) do nothing;

-- French Description (ID: 103)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (103, NOW(), NOW(),
        '<div>
           <h2>Eau de Parfum Black Opium Yves Saint Laurent</h2>
           <p>L''adrénaline du café noir et la sensualité des fleurs blanches. Un parfum vibrant, sensuel et addictif pour une femme rock et glamour.</p>
           <h3>Composition Olfactive :</h3>
           <ul>
             <li><strong>Note de Tête :</strong> Accord Café Noir</li>
             <li><strong>Note de Cœur :</strong> Fleur d''Oranger Absolu</li>
             <li><strong>Note de Fond :</strong> Cèdre, Patchouli, Vanille</li>
           </ul>
           <h3>Le Flacon :</h3>
           <p>Noir électrique pailleté, cœur incandescent, lignes radicales... Troublant, fascinant, à la texture pailletée unique, le flacon de Black Opium est l’objet du désir par excellence.</p>
           <h3>Spécifications :</h3>
           <ul>
             <li><strong>Type :</strong> Eau de Parfum</li>
             <li><strong>Famille Olfactive :</strong> Café Floral Gourmand</li>
             <li><strong>Contenance :</strong> 50ml (Exemple)</li>
           </ul>
         </div>',
        'YSL Black Opium Eau de Parfum', 'Yves Saint Laurent Black Opium Eau de Parfum - Parfum Café Floral Femme',
        'Vibrez avec Black Opium d''YSL, l''eau de parfum addictive au café noir et fleurs blanches.',
        'YSL, Black Opium, eau de parfum, parfum femme, café, floral, vanille, addictif',
        'YSL Black Opium Eau de Parfum | Café Floral Addictif', 'Café noir & Fleurs blanches',
        'ysl-black-opium-eau-de-parfum', 'fr', 52)
on conflict (description_id) do nothing;

-- English Description (ID: 104)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (104, NOW(), NOW(),
        '<div>
           <h2>Yves Saint Laurent Black Opium Eau de Parfum</h2>
           <p>The adrenaline of black coffee and the sensuality of white flowers. A vibrant, sensual, and addictive fragrance for a rock and glamorous woman.</p>
           <h3>Olfactive Composition:</h3>
           <ul>
             <li><strong>Top Note:</strong> Black Coffee Accord</li>
             <li><strong>Heart Note:</strong> Orange Blossom Absolute</li>
             <li><strong>Base Note:</strong> Cedarwood, Patchouli, Vanilla</li>
           </ul>
           <h3>The Bottle:</h3>
           <p>Glittering electric black, incandescent heart, radical lines... Unsettling, fascinating, with a unique glittery texture, the Black Opium bottle is the object of desire par excellence.</p>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Type:</strong> Eau de Parfum</li>
             <li><strong>Olfactive Family:</strong> Coffee Floral Gourmand</li>
             <li><strong>Volume:</strong> 50ml (Example)</li>
           </ul>
         </div>',
        'YSL Black Opium Eau de Parfum',
        'Yves Saint Laurent Black Opium Eau de Parfum - Women''s Coffee Floral Perfume',
        'Vibrate with YSL''s Black Opium, the addictive eau de parfum with black coffee and white flowers.',
        'YSL, Black Opium, eau de parfum, women perfume, coffee, floral, vanilla, addictive',
        'YSL Black Opium Eau de Parfum | Addictive Coffee Floral', 'Black coffee & White flowers',
        'ysl-black-opium-eau-de-parfum-en', 'en', 52)
on conflict (description_id) do nothing;

-- Product 53: Guerlain Terracotta Bronzing Powder (Manuf: 8, Type: 6)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (53, NOW(), NOW(), true, NOW(), false, 2.5,
        false, null, true, false, 0.08, 8.0, -- Compact dimensions
        'REF-GUER-MAKE-TERRACOTTA53', 53, 8,
        '65f020632bc46470c104b76f', 6)
on conflict (product_id) do nothing;

-- French Description (ID: 105)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (105, NOW(), NOW(),
        '<div>
           <h2>Poudre Bronzante Terracotta Guerlain</h2>
           <p>La poudre bronzante mythique de Guerlain pour un hâle naturel et ensoleillé toute l''année. Formule enrichie en actifs hydratants pour un confort longue durée.</p>
           <h3>Bénéfices :</h3>
           <ul>
             <li><strong>Hâle Naturel :</strong> Reproduit un bronzage léger et lumineux.</li>
             <li><strong>Texture Fine :</strong> Se fond parfaitement à la peau sans laisser de traces.</li>
             <li><strong>Confort & Hydratation :</strong> Formule infusée d''huile d''argan naturelle du Maroc.</li>
             <li><strong>Parfum Iconique :</strong> Notes solaires d''Ylang-Ylang, Fleur d''Oranger, Vanille et Muscs Blancs.</li>
             <li><strong>96% d''Ingrédients d''Origine Naturelle* :</strong> Respecte la peau.</li>
           </ul>
           <h3>Conseils d''application :</h3>
           <p>Appliquer en formant un "3" de chaque côté du visage : du front aux pommettes, puis des pommettes au menton, et enfin en redescendant le long du cou.</p>
           <h3>Spécifications :</h3>
           <ul>
             <li><strong>Gamme :</strong> Terracotta</li>
             <li><strong>Type :</strong> Poudre Bronzante</li>
             <li><strong>Teinte :</strong> Plusieurs teintes (Exemple: 03 Naturel - Brunettes)</li>
             <li><strong>Contenance :</strong> 10g</li>
           </ul>
           <p><small>*Conformément à la norme ISO 16128.</small></p>
         </div>',
        'Guerlain Terracotta Poudre Bronzante', 'Guerlain Terracotta Poudre Bronzante Hâle Naturel Longue Tenue',
        'Obtenez un hâle naturel toute l''année avec la poudre bronzante iconique Terracotta de Guerlain.',
        'Guerlain, Terracotta, poudre bronzante, bronzer, maquillage teint, hâle naturel',
        'Guerlain Terracotta Poudre Bronzante | Hâle Naturel', '96% d''ingrédients d''origine naturelle',
        'guerlain-terracotta-bronzing-powder', 'fr', 53)
on conflict (description_id) do nothing;

-- English Description (ID: 106)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (106, NOW(), NOW(),
        '<div>
           <h2>Guerlain Terracotta Bronzing Powder</h2>
           <p>Guerlain''s legendary bronzing powder for a natural, sun-kissed glow all year round. Formula enriched with hydrating active ingredients for long-lasting comfort.</p>
           <h3>Benefits:</h3>
           <ul>
             <li><strong>Natural Glow:</strong> Reproduces a light and luminous tan.</li>
             <li><strong>Fine Texture:</strong> Blends seamlessly into the skin without streaking.</li>
             <li><strong>Comfort & Hydration:</strong> Formula infused with natural Moroccan argan oil.</li>
             <li><strong>Iconic Fragrance:</strong> Solar notes of Ylang-Ylang, Orange Blossom, Vanilla, and White Musks.</li>
             <li><strong>96% Naturally-Derived Ingredients*:</strong> Respects the skin.</li>
           </ul>
           <h3>Application Tips:</h3>
           <p>Apply by tracing a "3" on each side of the face: from the forehead to the cheekbones, then from the cheekbones to the chin, and finally down the neck.</p>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Range:</strong> Terracotta</li>
             <li><strong>Type:</strong> Bronzing Powder</li>
             <li><strong>Shade:</strong> Several shades (Example: 03 Natural - Brunettes)</li>
             <li><strong>Content:</strong> 10g</li>
           </ul>
           <p><small>*In accordance with ISO 16128 standard.</small></p>
         </div>',
        'Guerlain Terracotta Bronzing Powder', 'Guerlain Terracotta Natural Healthy Glow Long-Lasting Bronzing Powder',
        'Get a natural sun-kissed glow all year round with Guerlain''s iconic Terracotta bronzing powder.',
        'Guerlain, Terracotta, bronzing powder, bronzer, face makeup, natural glow',
        'Guerlain Terracotta Bronzing Powder | Natural Glow', '96% naturally-derived ingredients',
        'guerlain-terracotta-bronzing-powder-en', 'en', 53)
on conflict (description_id) do nothing;

-- Product 54: Shiseido Benefiance Wrinkle Smoothing Cream (Manuf: 9, Type: 5)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (54, NOW(), NOW(), true, NOW(), false, 7.0,
        false, null, true, false, 0.25, 7.5, -- Jar dimensions
        'REF-SHIS-SKIN-BENEFCR54', 54, 9,
        '65f020632bc46470c104b76f', 5)
on conflict (product_id) do nothing;

-- French Description (ID: 107)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (107, NOW(), NOW(),
        '<div>
           <h2>Crème Lissante Anti-Rides Benefiance Shiseido</h2>
           <p>Une crème anti-rides soyeuse qui rend la peau plus réceptive aux bienfaits des soins, pour une apparence plus jeune et repulpée.</p>
           <h3>Technologie ReNeura+™ :</h3>
           <ul>
             <li><strong>Réveille les Capteurs Cutanés :</strong> Améliore la capacité de la peau à s''auto-régénérer.</li>
             <li><strong>Complexe KOMBU-Bounce :</strong> Cible la formation des rides de l''intérieur.</li>
             <li><strong>Hydratation Intense :</strong> Repulpe les ridules de sécheresse.</li>
             <li><strong>Texture Soyeuse :</strong> Pénètre rapidement pour un confort immédiat.</li>
           </ul>
           <h3>Résultats :</h3>
           <p>Les rides sont visiblement lissées, la peau est hydratée, repulpée et éclatante de jeunesse.</p>
           <h3>Spécifications :</h3>
           <ul>
             <li><strong>Gamme :</strong> Benefiance</li>
             <li><strong>Type :</strong> Crème Anti-Rides</li>
             <li><strong>Contenance :</strong> 50ml</li>
             <li><strong>Type de peau :</strong> Normale à Sèche</li>
           </ul>
         </div>',
        'Shiseido Benefiance Crème Lissante Anti-Rides',
        'Shiseido Benefiance Wrinkle Smoothing Cream Crème Anti-Rides Hydratante',
        'Lissez visiblement les rides avec la crème Benefiance de Shiseido, pour une peau plus jeune et réceptive.',
        'Shiseido, Benefiance, crème anti-rides, soin visage, lissant, hydratant, ReNeura',
        'Crème Anti-Rides Shiseido Benefiance | Lissante', 'Technologie ReNeura+™',
        'shiseido-benefiance-wrinkle-smoothing-cream', 'fr', 54)
on conflict (description_id) do nothing;

-- English Description (ID: 108)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (108, NOW(), NOW(),
        '<div>
           <h2>Shiseido Benefiance Wrinkle Smoothing Cream</h2>
           <p>A silky anti-wrinkle cream that makes skin more responsive to the benefits of skincare, for a plumper, younger-looking appearance.</p>
           <h3>ReNeura Technology+™:</h3>
           <ul>
             <li><strong>Awakens Skin Sensors:</strong> Improves the skin''s ability to self-regenerate.</li>
             <li><strong>KOMBU-Bounce Complex:</strong> Targets the formation of wrinkles from within.</li>
             <li><strong>Intense Hydration:</strong> Plumps up fine dry lines.</li>
             <li><strong>Silky Texture:</strong> Absorbs quickly for immediate comfort.</li>
           </ul>
           <h3>Results:</h3>
           <p>Wrinkles are visibly smoothed, skin is hydrated, plumped, and youthfully radiant.</p>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Range:</strong> Benefiance</li>
             <li><strong>Type:</strong> Anti-Wrinkle Cream</li>
             <li><strong>Volume:</strong> 50ml</li>
             <li><strong>Skin Type:</strong> Normal to Dry</li>
           </ul>
         </div>',
        'Shiseido Benefiance Wrinkle Smoothing Cream',
        'Shiseido Benefiance Wrinkle Smoothing Hydrating Anti-Wrinkle Cream',
        'Visibly smooth wrinkles with Shiseido''s Benefiance cream for younger, more responsive skin.',
        'Shiseido, Benefiance, anti-wrinkle cream, face care, smoothing, hydrating, ReNeura',
        'Anti-Wrinkle Cream Shiseido Benefiance | Smoothing', 'ReNeura Technology+™',
        'shiseido-benefiance-wrinkle-smoothing-cream-en', 'en', 54)
on conflict (description_id) do nothing;

-- Product 55: NARS Orgasm Blush (Manuf: 10, Type: 6)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (55, NOW(), NOW(), true, NOW(), false, 1.5,
        false, null, true, false, 0.05, 6.5, -- Compact dimensions
        'REF-NARS-MAKE-ORGBLUSH55', 55, 10,
        '65f020632bc46470c104b76f', 6)
on conflict (product_id) do nothing;

-- French Description (ID: 109)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (109, NOW(), NOW(),
        '<div>
           <h2>Blush Orgasm NARS</h2>
           <p>Le blush culte primé qui offre une touche de couleur naturelle et bonne mine. La teinte iconique rose pêche aux reflets dorés convient à toutes les carnations.</p>
           <h3>Caractéristiques :</h3>
           <ul>
             <li><strong>Couleur Iconique :</strong> Rose pêche universellement flatteur avec des reflets dorés subtils.</li>
             <li><strong>Fini Naturel :</strong> Apporte un éclat sain et lumineux.</li>
             <li><strong>Texture Soyeuse :</strong> Poudre micronisée ultra-fine pour une application facile et modulable.</li>
             <li><strong>Longue Tenue :</strong> Couleur qui tient sans s''estomper.</li>
           </ul>
           <h3>Conseils d''application :</h3>
           <p>Sourire pour repérer le bombé des pommettes. Appliquer le blush sur les pommettes en remontant vers les tempes.</p>
           <h3>Spécifications :</h3>
           <ul>
             <li><strong>Gamme :</strong> Blush</li>
             <li><strong>Teinte :</strong> Orgasm (Rose pêche doré)</li>
             <li><strong>Contenance :</strong> 4.8g</li>
           </ul>
         </div>',
        'NARS Blush Orgasm', 'NARS Blush Poudre Iconique Teinte Orgasm',
        'Obtenez une bonne mine naturelle avec le blush culte Orgasm de NARS, un rose pêche aux reflets dorés.',
        'NARS, Orgasm, blush, fard à joues, maquillage teint, rose pêche, éclat',
        'NARS Blush Orgasm | Fard à Joues Iconique', 'Teinte rose pêche universelle', 'nars-orgasm-blush', 'fr', 55)
on conflict (description_id) do nothing;

-- English Description (ID: 110)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (110, NOW(), NOW(),
        '<div>
           <h2>NARS Orgasm Blush</h2>
           <p>The award-winning cult-favorite blush that delivers a natural, healthy-looking flush of color. The iconic peachy-pink shade with golden shimmer suits all skin tones.</p>
           <h3>Features:</h3>
           <ul>
             <li><strong>Iconic Color:</strong> Universally flattering peachy-pink with subtle golden shimmer.</li>
             <li><strong>Natural Finish:</strong> Provides a healthy, luminous glow.</li>
             <li><strong>Silky Texture:</strong> Ultra-fine micronized powder for easy, buildable application.</li>
             <li><strong>Long-Wearing:</strong> Color stays true without fading.</li>
           </ul>
           <h3>Application Tips:</h3>
           <p>Smile to locate the apples of the cheeks. Apply blush to the apples, blending upwards towards the temples.</p>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Range:</strong> Blush</li>
             <li><strong>Shade:</strong> Orgasm (Peachy pink with golden shimmer)</li>
             <li><strong>Content:</strong> 4.8g</li>
           </ul>
         </div>',
        'NARS Orgasm Blush', 'NARS Iconic Powder Blush Shade Orgasm',
        'Get a natural healthy glow with NARS'' cult-favorite Orgasm blush, a peachy-pink with golden shimmer.',
        'NARS, Orgasm, blush, cheek color, face makeup, peachy pink, glow', 'NARS Orgasm Blush | Iconic Cheek Color',
        'Universal peachy-pink shade', 'nars-orgasm-blush-en', 'en', 55)
on conflict (description_id) do nothing;

-- Product 56: La Roche-Posay Cicaplast Baume B5 (Manuf: 11, Type: 5)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (56, NOW(), NOW(), true, NOW(), false, 17.0,
        false, null, true, false, 0.12, 5.0, -- Approx for 100ml tube
        'REF-LRP-SKIN-CICAB5-56', 56, 11,
        '65f020632bc46470c104b76f', 5)
on conflict (product_id) do nothing;

-- French Description (ID: 111)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (111, NOW(), NOW(),
        '<div>
           <h2>Baume Cicaplast B5 La Roche-Posay</h2>
           <p>Le baume multi-réparateur apaisant pour les irritations et échauffements cutanés de l''adulte, l''enfant et du nourrisson. Répare, apaise et protège la peau.</p>
           <h3>Bénéfices :</h3>
           <ul>
             <li><strong>Réparation Cutanée :</strong> Madécassoside + Cuivre-Zinc-Manganèse pour une réparation épidermique optimale.</li>
             <li><strong>Apaisement Immédiat :</strong> Panthénol concentré à 5% pour soulager intensément.</li>
             <li><strong>Protection Anti-Marques :</strong> Texture baume riche et nourrissante avec agents antibactériens.</li>
             <li><strong>Haute Tolérance :</strong> Convient aux peaux les plus sensibles (visage, corps, lèvres). Sans parfum, sans paraben.</li>
           </ul>
           <h3>Indications :</h3>
           <p>Irritations (dartres, gerçures), échauffements, rougeurs du siège, post-interventions dermatologiques (laser, peeling).</p>
           <h3>Spécifications :</h3>
           <ul>
             <li><strong>Gamme :</strong> Cicaplast</li>
             <li><strong>Contenance :</strong> 100ml (Exemple)</li>
             <li><strong>Texture :</strong> Baume riche, non gras, non collant.</li>
           </ul>
         </div>',
        'La Roche-Posay Cicaplast Baume B5', 'La Roche-Posay Cicaplast Baume B5 Réparateur Apaisant Anti-Marques',
        'Réparez et apaisez les peaux irritées avec Cicaplast Baume B5 de La Roche-Posay, le soin SOS multi-usage.',
        'La Roche-Posay, Cicaplast, Baume B5, réparateur, apaisant, irritations, peau sensible, panthénol',
        'Baume Réparateur La Roche-Posay Cicaplast B5 | Apaisant', 'Répare, apaise, protège',
        'la-roche-posay-cicaplast-baume-b5', 'fr', 56)
on conflict (description_id) do nothing;

-- English Description (ID: 112)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (112, NOW(), NOW(),
        '<div>
           <h2>La Roche-Posay Cicaplast Baume B5</h2>
           <p>The multi-repairing soothing balm for skin irritations and heating sensations in adults, children, and babies. Repairs, soothes, and protects the skin.</p>
           <h3>Benefits:</h3>
           <ul>
             <li><strong>Skin Repair:</strong> Madecassoside + Copper-Zinc-Manganese for optimal epidermal repair.</li>
             <li><strong>Immediate Soothing:</strong> Panthenol concentrated at 5% to intensely relieve.</li>
             <li><strong>Anti-Mark Protection:</strong> Rich, nourishing balm texture with antibacterial agents.</li>
             <li><strong>High Tolerance:</strong> Suitable for the most sensitive skin (face, body, lips). Fragrance-free, paraben-free.</li>
           </ul>
           <h3>Indications:</h3>
           <p>Irritations (chapping, cracks), heating sensations, diaper rash, post-dermatological procedures (laser, peeling).</p>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Range:</strong> Cicaplast</li>
             <li><strong>Volume:</strong> 100ml (Example)</li>
             <li><strong>Texture:</strong> Rich balm, non-greasy, non-sticky.</li>
           </ul>
         </div>',
        'La Roche-Posay Cicaplast Baume B5', 'La Roche-Posay Cicaplast Baume B5 Soothing Repairing Anti-Mark Balm',
        'Repair and soothe irritated skin with La Roche-Posay Cicaplast Baume B5, the multi-purpose SOS care.',
        'La Roche-Posay, Cicaplast, Baume B5, repairing, soothing, irritations, sensitive skin, panthenol',
        'Repairing Balm La Roche-Posay Cicaplast B5 | Soothing', 'Repairs, soothes, protects',
        'la-roche-posay-cicaplast-baume-b5-en', 'en', 56)
on conflict (description_id) do nothing;

-- Product 57: Kérastase Elixir Ultime Original Hair Oil (Manuf: 12, Type: 8)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (57, NOW(), NOW(), true, NOW(), false, 16.0,
        false, null, true, false, 0.3, 5.5,
        'REF-KERA-HAIR-ELIXIR57', 57, 12,
        '65f020632bc46470c104b76f', 8)
on conflict (product_id) do nothing;

-- French Description (ID: 113)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (113, NOW(), NOW(),
        '<div>
           <h2>Huile Originale Elixir Ultime Kérastase</h2>
           <p>L''huile capillaire iconique multi-usages pour tous types de cheveux. Nourrit, sublime la brillance et protège de la chaleur jusqu''à 230°C.</p>
           <h3>Bénéfices :</h3>
           <ul>
             <li><strong>Brillance Intense :</strong> Apporte une brillance immédiate et durable.</li>
             <li><strong>Nutrition Profonde :</strong> Enrichie en Huile Sacrée de Marula et Huile de Camélia Précieux.</li>
             <li><strong>Action Anti-Frisottis :</strong> Contrôle les frisottis jusqu''à 96 heures*.</li>
             <li><strong>Protection Thermique :</strong> Protège de la chaleur des outils coiffants jusqu''à 230°C.</li>
             <li><strong>Parfum Envoûtant :</strong> Signature olfactive florale et boisée.</li>
           </ul>
           <h3>Conseils d''utilisation :</h3>
           <p>Appliquer 1 à 2 pressions sur cheveux secs ou humides, sur les longueurs et pointes. Ne pas rincer. Peut être utilisée avant le shampooing, avant le brushing ou en finition.</p>
           <h3>Spécifications :</h3>
           <ul>
             <li><strong>Gamme :</strong> Elixir Ultime</li>
             <li><strong>Contenance :</strong> 100ml</li>
             <li><strong>Type de cheveux :</strong> Tous types</li>
           </ul>
           <p><small>*Test instrumental.</small></p>
         </div>',
        'Kérastase Elixir Ultime Huile Originale',
        'Kérastase Elixir Ultime L''Huile Originale Huile Capillaire Sublimatrice',
        'Sublimez vos cheveux avec l''Huile Originale Elixir Ultime de Kérastase pour une brillance et une nutrition intenses.',
        'Kérastase, Elixir Ultime, huile cheveux, huile capillaire, brillance, nutrition, anti-frisottis, protection thermique',
        'Huile Kérastase Elixir Ultime | Brillance & Nutrition', 'Brillance intense & Anti-frisottis',
        'kerastase-elixir-ultime-original-hair-oil', 'fr', 57)
on conflict (description_id) do nothing;

-- English Description (ID: 114)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (114, NOW(), NOW(),
        '<div>
           <h2>Kérastase Elixir Ultime Original Hair Oil</h2>
           <p>The iconic multi-use hair oil for all hair types. Nourishes, enhances shine, and provides heat protection up to 230°C.</p>
           <h3>Benefits:</h3>
           <ul>
             <li><strong>Intense Shine:</strong> Provides immediate and long-lasting shine.</li>
             <li><strong>Deep Nutrition:</strong> Enriched with Sacred Marula Oil and Precious Camellia Oil.</li>
             <li><strong>Anti-Frizz Action:</strong> Controls frizz for up to 96 hours*.</li>
             <li><strong>Heat Protection:</strong> Protects from heat styling tools up to 230°C.</li>
             <li><strong>Captivating Fragrance:</strong> Floral and woody olfactory signature.</li>
           </ul>
           <h3>How to Use:</h3>
           <p>Apply 1 to 2 pumps on dry or damp hair, on lengths and ends. Do not rinse. Can be used before shampooing, before blow-drying, or as a finishing touch.</p>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Range:</strong> Elixir Ultime</li>
             <li><strong>Volume:</strong> 100ml</li>
             <li><strong>Hair Type:</strong> All types</li>
           </ul>
           <p><small>*Instrumental test.</small></p>
         </div>',
        'Kérastase Elixir Ultime Original Hair Oil', 'Kérastase Elixir Ultime L''Huile Originale Beautifying Hair Oil',
        'Enhance your hair with Kérastase Elixir Ultime Original Oil for intense shine and nutrition.',
        'Kérastase, Elixir Ultime, hair oil, shine, nutrition, anti-frizz, heat protection',
        'Kérastase Elixir Ultime Hair Oil | Shine & Nutrition', 'Intense shine & Anti-frizz',
        'kerastase-elixir-ultime-original-hair-oil-en', 'en', 57)
on conflict (description_id) do nothing;

-- Product 58: YSL Libre Eau de Parfum (Manuf: 7, Type: 7)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (58, NOW(), NOW(), true, NOW(), false, 11.0,
        false, null, true, false, 0.32, 6.0, -- Approx for 50ml
        'REF-YSL-FRAG-LIBREEDP58', 58, 7,
        '65f020632bc46470c104b76f', 7)
on conflict (product_id) do nothing;

-- French Description (ID: 115)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (115, NOW(), NOW(),
        '<div>
           <h2>Eau de Parfum Libre Yves Saint Laurent</h2>
           <p>La nouvelle Eau de Parfum par Yves Saint Laurent, la liberté de vivre tout terriblement. Le parfum d''une femme forte, audacieuse et libre.</p>
           <h3>La Tension Olfactive :</h3>
           <p>Une lavande florale, sensuelle et audacieuse. La tension entre la sensualité brûlante de la fleur d''oranger du Maroc et l''audace d''une lavande de France revisitée au féminin.</p>
           <h3>Notes Clés :</h3>
           <ul>
             <li><strong>Essence de Néroli & Essence de Mandarine</strong></li>
             <li><strong>Absolu Fleur d''Oranger du Maroc & Essence de Lavande de France</strong></li>
             <li><strong>Extrait de Vanille de Madagascar & Essence de Cèdre</strong></li>
           </ul>
           <h3>Le Flacon :</h3>
           <p>Un flacon couture torsadé par le Cassandre doré et luxueux, logo iconique de la marque.</p>
           <h3>Spécifications :</h3>
           <ul>
             <li><strong>Type :</strong> Eau de Parfum</li>
             <li><strong>Famille Olfactive :</strong> Floral Lavande</li>
             <li><strong>Contenance :</strong> 50ml (Exemple)</li>
           </ul>
         </div>',
        'YSL Libre Eau de Parfum', 'Yves Saint Laurent Libre Eau de Parfum - Parfum Floral Lavande Femme',
        'Exprimez votre liberté avec Libre d''YSL, l''eau de parfum florale lavande audacieuse et sensuelle.',
        'YSL, Libre, eau de parfum, parfum femme, floral, lavande, fleur oranger, liberté',
        'YSL Libre Eau de Parfum | Floral Lavande Audacieux', 'Lavande florale audacieuse', 'ysl-libre-eau-de-parfum',
        'fr', 58)
on conflict (description_id) do nothing;

-- English Description (ID: 116)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (116, NOW(), NOW(),
        '<div>
           <h2>Yves Saint Laurent Libre Eau de Parfum</h2>
           <p>The new Eau de Parfum by Yves Saint Laurent, the freedom to live everything with excess. The fragrance of a strong, bold, and free woman.</p>
           <h3>The Olfactory Tension:</h3>
           <p>A floral lavender, sensual and bold. The tension between the burning sensuality of orange blossom from Morocco and the boldness of a French lavender twisted with a feminine touch.</p>
           <h3>Key Notes:</h3>
           <ul>
             <li><strong>Neroli Essence & Mandarin Essence</strong></li>
             <li><strong>Moroccan Orange Blossom Absolute & French Lavender Essence</strong></li>
             <li><strong>Madagascar Vanilla Extract & Cedarwood Essence</strong></li>
           </ul>
           <h3>The Bottle:</h3>
           <p>A couture bottle twisted by the luxurious oversized golden Cassandre, the brand''s iconic logo.</p>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Type:</strong> Eau de Parfum</li>
             <li><strong>Olfactive Family:</strong> Floral Lavender</li>
             <li><strong>Volume:</strong> 50ml (Example)</li>
           </ul>
         </div>',
        'YSL Libre Eau de Parfum', 'Yves Saint Laurent Libre Eau de Parfum - Women''s Floral Lavender Perfume',
        'Express your freedom with YSL''s Libre, the bold and sensual floral lavender eau de parfum.',
        'YSL, Libre, eau de parfum, women perfume, floral, lavender, orange blossom, freedom',
        'YSL Libre Eau de Parfum | Bold Floral Lavender', 'Bold floral lavender', 'ysl-libre-eau-de-parfum-en', 'en',
        58)
on conflict (description_id) do nothing;

-- Product 59: Guerlain Météorites Pearls (Manuf: 8, Type: 6)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (59, NOW(), NOW(), true, NOW(), false, 6.0,
        false, null, true, false, 0.1, 7.0, -- Compact dimensions
        'REF-GUER-MAKE-METEORITES59', 59, 8,
        '65f020632bc46470c104b76f', 6)
on conflict (product_id) do nothing;

-- French Description (ID: 117)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (117, NOW(), NOW(),
        '<div>
           <h2>Perles Météorites Guerlain</h2>
           <p>Les perles de poudre mythiques qui révèlent l''éclat du teint. Une constellation de perles multicolores pour corriger, illuminer et fixer le maquillage.</p>
           <h3>Technologie Poussière d''Étoiles :</h3>
           <ul>
             <li><strong>Harmonie de Couleurs :</strong> Perles roses pour rafraîchir, mauves pour accrocher la lumière, vertes pour atténuer les rougeurs, blanches pour illuminer, dorées pour réchauffer.</li>
             <li><strong>Fini Lumineux :</strong> Diffuse une lumière pure et correctrice pour un éclat sur mesure.</li>
             <li><strong>Texture Aérienne :</strong> Poudre ultra-légère qui fixe le maquillage sans effet matière.</li>
             <li><strong>Parfum Violette :</strong> Parfum délicat et addictif.</li>
           </ul>
           <h3>Conseils d''utilisation :</h3>
           <p>Balayer les perles avec le Pinceau Météorites puis appliquer uniformément sur l''ensemble du visage pour fixer le maquillage et améliorer la tenue.</p>
           <h3>Spécifications :</h3>
           <ul>
             <li><strong>Gamme :</strong> Météorites</li>
             <li><strong>Type :</strong> Poudre Illuminatrice en Perles</li>
             <li><strong>Teinte :</strong> Plusieurs harmonies (Exemple: 02 Clair)</li>
             <li><strong>Contenance :</strong> 25g</li>
           </ul>
         </div>',
        'Guerlain Météorites Perles de Poudre Révélatrices de Lumière',
        'Guerlain Météorites Light Revealing Pearls of Powder',
        'Révélez l''éclat de votre teint avec les perles Météorites de Guerlain, la poudre illuminatrice iconique.',
        'Guerlain, Météorites, perles, poudre illuminatrice, éclat, maquillage teint, finition',
        'Guerlain Météorites Perles | Poudre Illuminatrice', 'Éclat pur et correcteur', 'guerlain-meteorites-pearls',
        'fr', 59)
on conflict (description_id) do nothing;

-- English Description (ID: 118)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (118, NOW(), NOW(),
        '<div>
           <h2>Guerlain Météorites Pearls</h2>
           <p>The mythical pearls of powder that reveal the complexion''s radiance. A constellation of multi-colored pearls to correct, illuminate, and set makeup.</p>
           <h3>Stardust Technology:</h3>
           <ul>
             <li><strong>Color Harmony:</strong> Pink pearls to refresh, mauve to catch the light, green to reduce redness, white to illuminate, gold to warm up.</li>
             <li><strong>Luminous Finish:</strong> Diffuses pure, corrective light for tailored radiance.</li>
             <li><strong>Airy Texture:</strong> Ultra-light powder that sets makeup without cakiness.</li>
             <li><strong>Violet Scent:</strong> Delicate and addictive fragrance.</li>
           </ul>
           <h3>How to Use:</h3>
           <p>Sweep the Météorites Brush over the pearls then apply evenly to the entire face to set makeup and improve hold.</p>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Range:</strong> Météorites</li>
             <li><strong>Type:</strong> Light Revealing Pearls of Powder</li>
             <li><strong>Shade:</strong> Several harmonies (Example: 02 Clair/Light)</li>
             <li><strong>Content:</strong> 25g</li>
           </ul>
         </div>',
        'Guerlain Météorites Light Revealing Pearls of Powder', 'Guerlain Météorites Light Revealing Pearls of Powder',
        'Reveal your complexion''s radiance with Guerlain Météorites pearls, the iconic illuminating powder.',
        'Guerlain, Météorites, pearls, illuminating powder, radiance, face makeup, finishing powder',
        'Guerlain Météorites Pearls | Illuminating Powder', 'Pure corrective radiance', 'guerlain-meteorites-pearls-en',
        'en', 59)
on conflict (description_id) do nothing;

-- Product 60: Shiseido Synchro Skin Self-Refreshing Foundation (Manuf: 9, Type: 6)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (60, NOW(), NOW(), true, NOW(), false, 12.5,
        false, null, true, false, 0.13, 4.0,
        'REF-SHIS-MAKE-SYNCSKIN60', 60, 9,
        '65f020632bc46470c104b76f', 6)
on conflict (product_id) do nothing;

-- French Description (ID: 119)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (119, NOW(), NOW(),
        '<div>
           <h2>Fond de Teint Synchro Skin Self-Refreshing Shiseido</h2>
           <p>Un fond de teint intelligent qui se synchronise avec la peau et s''auto-rafraîchit pour un fini impeccable et frais toute la journée. Tenue 24h, résistant à l''eau et à l''humidité.</p>
           <h3>Technologie ActiveForce™ :</h3>
           <ul>
             <li><strong>Auto-Rafraîchissant :</strong> Résiste à la chaleur, l''humidité, l''excès de sébum et aux mouvements du visage.</li>
             <li><strong>Synchronisation Cutanée :</strong> S''adapte à l''état et au teint de la peau.</li>
             <li><strong>Couvrance Modulable :</strong> Fini naturel, léger et respirant.</li>
             <li><strong>Protection SPF 30 :</strong> Protège des rayons UV.</li>
           </ul>
           <h3>Résultats :</h3>
           <p>Un teint frais, naturel et unifié qui dure toute la journée, comme si le maquillage venait d''être appliqué.</p>
           <h3>Spécifications :</h3>
           <ul>
             <li><strong>Gamme :</strong> Synchro Skin</li>
             <li><strong>Type :</strong> Fond de Teint Fluide Longue Tenue</li>
             <li><strong>Contenance :</strong> 30ml</li>
             <li><strong>Teinte :</strong> Large gamme (Exemple: 220 Linen)</li>
             <li><strong>Formule :</strong> Non comédogène, testé dermatologiquement.</li>
           </ul>
         </div>',
        'Shiseido Synchro Skin Self-Refreshing Foundation',
        'Shiseido Synchro Skin Fond de Teint Auto-Rafraîchissant Tenue 24h SPF 30',
        'Découvrez le fond de teint intelligent Synchro Skin de Shiseido qui reste frais et impeccable 24h.',
        'Shiseido, Synchro Skin, fond de teint, maquillage teint, longue tenue, auto-rafraîchissant, SPF 30',
        'Fond de Teint Shiseido Synchro Skin | Auto-Rafraîchissant', 'Tenue 24h, Fini Frais',
        'shiseido-synchro-skin-self-refreshing-foundation', 'fr', 60)
on conflict (description_id) do nothing;

-- English Description (ID: 120)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (120, NOW(), NOW(),
        '<div>
           <h2>Shiseido Synchro Skin Self-Refreshing Foundation</h2>
           <p>An intelligent foundation that synchronizes with the skin and self-refreshes for a flawless, fresh finish all day. 24-hour wear, water-resistant, humidity-resistant.</p>
           <h3>ActiveForce™ Technology:</h3>
           <ul>
             <li><strong>Self-Refreshing:</strong> Resists heat, humidity, excess oil, and facial movements.</li>
             <li><strong>Skin Synchronization:</strong> Adapts to the skin''s condition and tone.</li>
             <li><strong>Buildable Coverage:</strong> Natural finish, lightweight and breathable.</li>
             <li><strong>SPF 30 Protection:</strong> Protects from UV rays.</li>
           </ul>
           <h3>Results:</h3>
           <p>A fresh, natural, and unified complexion that lasts all day, as if makeup has just been applied.</p>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Range:</strong> Synchro Skin</li>
             <li><strong>Type:</strong> Long-Wearing Fluid Foundation</li>
             <li><strong>Volume:</strong> 30ml</li>
             <li><strong>Shade:</strong> Wide range (Example: 220 Linen)</li>
             <li><strong>Formula:</strong> Non-comedogenic, dermatologist-tested.</li>
           </ul>
         </div>',
        'Shiseido Synchro Skin Self-Refreshing Foundation',
        'Shiseido Synchro Skin Self-Refreshing 24h Wear Foundation SPF 30',
        'Discover Shiseido''s intelligent Synchro Skin foundation that stays fresh and flawless for 24 hours.',
        'Shiseido, Synchro Skin, foundation, face makeup, long wear, self-refreshing, SPF 30',
        'Foundation Shiseido Synchro Skin | Self-Refreshing', '24h Wear, Fresh Finish',
        'shiseido-synchro-skin-self-refreshing-foundation-en', 'en', 60)
on conflict (description_id) do nothing;
/*
Generated SQL inserts similar to the provided template
where languages=['fr','en'] and store_id='65f020632bc46470c104b76f'
and manufacturer_id in (7,8,9,10,11,12) and product_type_id in (5,6,7,8)
start product id from 61
start product_description from 121
generate 15 products (61-75)
make sure to replace all ids with real number in sequence
use html format for description generator so it contain valid html tags describe the product and its features and specification and more
domain for this store is beauté
Timestamps replaced with NOW()
*/

-- Product 61: NARS Laguna Bronzing Powder (Manuf: 10, Type: 6)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (61, NOW(), NOW(), true, NOW(), false, 1.8,
        false, null, true, false, 0.07, 7.5, -- Compact dimensions
        'REF-NARS-MAKE-LAGUNABRZ61', 61, 10,
        '65f020632bc46470c104b76f', 6)
on conflict (product_id) do nothing;

-- French Description (ID: 121)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (121, NOW(), NOW(),
        '<div>
           <h2>Poudre Bronzante Laguna NARS</h2>
           <p>La poudre bronzante iconique qui crée l''illusion ultime d''une peau hâlée et en pleine santé. La teinte Laguna offre un brun moyen diffus aux reflets dorés subtils.</p>
           <h3>Bénéfices :</h3>
           <ul>
             <li><strong>Hâle Sur Mesure :</strong> Des poudres finement broyées pour un fini lisse et estompable.</li>
             <li><strong>Éclat Doré :</strong> Des particules irisées subtiles pour un effet lumineux naturel.</li>
             <li><strong>Texture Veloutée :</strong> Se fond sans effort sur la peau pour un résultat sans traces.</li>
             <li><strong>Teinte Universelle :</strong> Laguna convient à une large gamme de carnations pour réchauffer le teint.</li>
           </ul>
           <h3>Conseils d''application :</h3>
           <p>Appliquer sur les zones où le soleil touche naturellement le visage : front, pommettes, arête du nez et menton.</p>
           <h3>Spécifications :</h3>
           <ul>
             <li><strong>Gamme :</strong> Bronzing Powder</li>
             <li><strong>Teinte :</strong> Laguna (Brun moyen diffus aux reflets dorés)</li>
             <li><strong>Contenance :</strong> 8g</li>
           </ul>
         </div>',
        'NARS Poudre Bronzante Laguna', 'NARS Poudre Bronzante Iconique Teinte Laguna',
        'Obtenez un hâle doré naturel avec la poudre bronzante Laguna de NARS, une teinte iconique.',
        'NARS, Laguna, poudre bronzante, bronzer, maquillage teint, hâle doré',
        'NARS Poudre Bronzante Laguna | Hâle Doré', 'Brun moyen diffus aux reflets dorés',
        'nars-laguna-bronzing-powder', 'fr', 61)
on conflict (description_id) do nothing;

-- English Description (ID: 122)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (122, NOW(), NOW(),
        '<div>
           <h2>NARS Laguna Bronzing Powder</h2>
           <p>The iconic bronzing powder that creates the ultimate illusion of healthy, sun-kissed skin. The Laguna shade offers a diffused medium brown with subtle golden shimmer.</p>
           <h3>Benefits:</h3>
           <ul>
             <li><strong>Customizable Glow:</strong> Finely milled powders for a smooth, blendable finish.</li>
             <li><strong>Golden Radiance:</strong> Subtle iridescent particles for a natural luminous effect.</li>
             <li><strong>Velvety Texture:</strong> Blends effortlessly onto the skin for a streak-free result.</li>
             <li><strong>Universal Shade:</strong> Laguna suits a wide range of skin tones to warm the complexion.</li>
           </ul>
           <h3>Application Tips:</h3>
           <p>Apply to areas where the sun naturally hits the face: forehead, cheekbones, bridge of the nose, and chin.</p>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Range:</strong> Bronzing Powder</li>
             <li><strong>Shade:</strong> Laguna (Diffused medium brown with golden shimmer)</li>
             <li><strong>Content:</strong> 8g</li>
           </ul>
         </div>',
        'NARS Laguna Bronzing Powder', 'NARS Iconic Bronzing Powder Shade Laguna',
        'Get a natural golden glow with NARS Laguna bronzing powder, an iconic shade.',
        'NARS, Laguna, bronzing powder, bronzer, face makeup, golden glow', 'NARS Laguna Bronzing Powder | Golden Glow',
        'Diffused medium brown with golden shimmer', 'nars-laguna-bronzing-powder-en', 'en', 61)
on conflict (description_id) do nothing;

-- Product 62: La Roche-Posay Hyalu B5 Serum (Manuf: 11, Type: 5)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (62, NOW(), NOW(), true, NOW(), false, 10.5,
        false, null, true, false, 0.1, 4.0,
        'REF-LRP-SKIN-HYALUB5-62', 62, 11,
        '65f020632bc46470c104b76f', 5)
on conflict (product_id) do nothing;

-- French Description (ID: 123)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (123, NOW(), NOW(),
        '<div>
           <h2>Sérum Hyalu B5 La Roche-Posay</h2>
           <p>Le sérum anti-rides concentré réparateur repulpant. Formulé avec deux types d''acides hyaluroniques purs, vitamine B5 et madécassoside pour repulper et réparer la barrière cutanée.</p>
           <h3>Bénéfices :</h3>
           <ul>
             <li><strong>Action Repulpante :</strong> Duo d''Acides Hyaluroniques (haut et bas poids moléculaire) pour hydrater en surface et en profondeur.</li>
             <li><strong>Réparation Cutanée :</strong> Vitamine B5 pour apaiser et Madécassoside pour stimuler la réparation.</li>
             <li><strong>Texture Aqua-Gel :</strong> Fraîche et hydratante, pénètre rapidement.</li>
             <li><strong>Haute Tolérance :</strong> Convient aux peaux sensibles, testé post-procédures.</li>
           </ul>
           <h3>Résultats :</h3>
           <p>Immédiatement, la peau est plus souple et hydratée. Les rides et ridules sont visiblement réduites, la peau retrouve son rebond.</p>
           <h3>Spécifications :</h3>
           <ul>
             <li><strong>Gamme :</strong> Hyalu B5</li>
             <li><strong>Contenance :</strong> 30ml</li>
             <li><strong>Type de peau :</strong> Tous types, idéal peaux sensibles, marquées par les rides.</li>
           </ul>
         </div>',
        'La Roche-Posay Hyalu B5 Sérum Anti-Rides',
        'La Roche-Posay Hyalu B5 Sérum Concentré Anti-Rides Réparateur Repulpant',
        'Repulpez et réparez votre peau avec le sérum Hyalu B5 de La Roche-Posay à l''acide hyaluronique et vitamine B5.',
        'La Roche-Posay, Hyalu B5, sérum, anti-rides, acide hyaluronique, vitamine B5, réparateur, repulpant, peau sensible',
        'Sérum La Roche-Posay Hyalu B5 | Anti-Rides & Réparateur', 'Acide Hyaluronique & Vitamine B5',
        'la-roche-posay-hyalu-b5-serum', 'fr', 62)
on conflict (description_id) do nothing;

-- English Description (ID: 124)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (124, NOW(), NOW(),
        '<div>
           <h2>La Roche-Posay Hyalu B5 Serum</h2>
           <p>The anti-wrinkle concentrate, repairing, replumping serum. Formulated with two types of pure hyaluronic acids, vitamin B5, and madecassoside to replump and repair the skin barrier.</p>
           <h3>Benefits:</h3>
           <ul>
             <li><strong>Replumping Action:</strong> Duo of Hyaluronic Acids (high and low molecular weight) to hydrate on the surface and in depth.</li>
             <li><strong>Skin Repair:</strong> Vitamin B5 to soothe and Madecassoside to stimulate repair.</li>
             <li><strong>Aqua-Gel Texture:</strong> Fresh and hydrating, absorbs quickly.</li>
             <li><strong>High Tolerance:</strong> Suitable for sensitive skin, tested post-procedure.</li>
           </ul>
           <h3>Results:</h3>
           <p>Immediately, skin is more supple and hydrated. Wrinkles and fine lines are visibly reduced, skin regains its bounce.</p>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Range:</strong> Hyalu B5</li>
             <li><strong>Volume:</strong> 30ml</li>
             <li><strong>Skin Type:</strong> All types, ideal for sensitive skin marked by wrinkles.</li>
           </ul>
         </div>',
        'La Roche-Posay Hyalu B5 Anti-Wrinkle Serum',
        'La Roche-Posay Hyalu B5 Anti-Wrinkle Repairing Replumping Concentrate Serum',
        'Replump and repair your skin with La Roche-Posay Hyalu B5 serum with hyaluronic acid and vitamin B5.',
        'La Roche-Posay, Hyalu B5, serum, anti-wrinkle, hyaluronic acid, vitamin B5, repairing, replumping, sensitive skin',
        'Serum La Roche-Posay Hyalu B5 | Anti-Wrinkle & Repairing', 'Hyaluronic Acid & Vitamin B5',
        'la-roche-posay-hyalu-b5-serum-en', 'en', 62)
on conflict (description_id) do nothing;

-- Product 63: Kérastase Blond Absolu Cicaflash Conditioner (Manuf: 12, Type: 8)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (63, NOW(), NOW(), true, NOW(), false, 19.0,
        false, null, true, false, 0.28, 5.0, -- Approx for 250ml tube
        'REF-KERA-HAIR-BACICAFLASH63', 63, 12,
        '65f020632bc46470c104b76f', 8)
on conflict (product_id) do nothing;

-- French Description (ID: 125)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (125, NOW(), NOW(),
        '<div>
           <h2>Fondant Cicaflash Blond Absolu Kérastase</h2>
           <p>Le soin fortifiant intense pour cheveux blonds décolorés ou méchés. La puissance réparatrice d''un masque avec la légèreté d''un après-shampooing.</p>
           <h3>Bénéfices :</h3>
           <ul>
             <li><strong>Réparation Intense :</strong> Restaure en profondeur la fibre sensibilisée par la décoloration.</li>
             <li><strong>Hydratation Légère :</strong> Hydrate sans alourdir pour un toucher doux et soyeux.</li>
             <li><strong>Protection & Brillance :</strong> Acide Hyaluronique et Fleur d''Edelweiss pour protéger et illuminer.</li>
             <li><strong>Démêlage Facilité :</strong> Facilite le coiffage et réduit la casse.</li>
           </ul>
           <h3>Conseils d''utilisation :</h3>
           <p>Appliquer sur cheveux lavés et essorés. Masser les longueurs et pointes. Laisser poser 2 à 3 minutes. Émulsionner puis rincer abondamment.</p>
           <h3>Spécifications :</h3>
           <ul>
             <li><strong>Gamme :</strong> Blond Absolu</li>
             <li><strong>Type de cheveux :</strong> Blonds, Décolorés, Méchés</li>
             <li><strong>Contenance :</strong> 250ml</li>
           </ul>
         </div>',
        'Kérastase Blond Absolu Fondant Cicaflash',
        'Kérastase Blond Absolu Cicaflash Soin Fortifiant Intense Cheveux Blonds',
        'Réparez et hydratez vos cheveux blonds avec le fondant Cicaflash Blond Absolu de Kérastase.',
        'Kérastase, Blond Absolu, Cicaflash, après-shampooing, soin cheveux blonds, décolorés, réparation, hydratation',
        'Soin Kérastase Blond Absolu Cicaflash | Cheveux Blonds', 'Répare et hydrate en légèreté',
        'kerastase-blond-absolu-cicaflash-conditioner', 'fr', 63)
on conflict (description_id) do nothing;

-- English Description (ID: 126)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (126, NOW(), NOW(),
        '<div>
           <h2>Kérastase Blond Absolu Cicaflash Conditioner</h2>
           <p>The intense fortifying treatment for lightened or highlighted blonde hair. The restorative power of a mask with the lightweight touch of a conditioner.</p>
           <h3>Benefits:</h3>
           <ul>
             <li><strong>Intense Repair:</strong> Deeply restores the fiber sensitized by bleaching.</li>
             <li><strong>Lightweight Hydration:</strong> Hydrates without weighing down for a soft, silky touch.</li>
             <li><strong>Protection & Shine:</strong> Hyaluronic Acid and Edelweiss Flower to protect and illuminate.</li>
             <li><strong>Easy Detangling:</strong> Facilitates styling and reduces breakage.</li>
           </ul>
           <h3>How to Use:</h3>
           <p>Apply to washed and towel-dried hair. Massage onto lengths and ends. Leave on for 2 to 3 minutes. Emulsify and rinse thoroughly.</p>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Range:</strong> Blond Absolu</li>
             <li><strong>Hair Type:</strong> Blonde, Lightened, Highlighted</li>
             <li><strong>Volume:</strong> 250ml</li>
           </ul>
         </div>',
        'Kérastase Blond Absolu Cicaflash Conditioner',
        'Kérastase Blond Absolu Cicaflash Intense Fortifying Treatment for Blonde Hair',
        'Repair and hydrate your blonde hair with Kérastase Blond Absolu Cicaflash conditioner.',
        'Kérastase, Blond Absolu, Cicaflash, conditioner, blonde hair care, lightened, repair, hydration',
        'Conditioner Kérastase Blond Absolu Cicaflash | Blonde Hair', 'Repairs and hydrates lightly',
        'kerastase-blond-absolu-cicaflash-conditioner-en', 'en', 63)
on conflict (description_id) do nothing;

-- Product 64: YSL Rouge Pur Couture Lipstick (Manuf: 7, Type: 6)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (64, NOW(), NOW(), true, NOW(), false, 7.5,
        false, null, true, false, 0.04, 2.0,
        'REF-YSL-MAKE-RPCLIP64', 64, 7,
        '65f020632bc46470c104b76f', 6)
on conflict (product_id) do nothing;

-- French Description (ID: 127)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (127, NOW(), NOW(),
        '<div>
           <h2>Rouge à Lèvres Rouge Pur Couture Yves Saint Laurent</h2>
           <p>Le rouge à lèvres iconique d''Yves Saint Laurent. Une gamme de teintes uniques inspirées des couleurs emblématiques d''YSL, dans un écrin couture doré.</p>
           <h3>Bénéfices :</h3>
           <ul>
             <li><strong>Couleur Intense & Pure :</strong> Pigments riches pour une couleur éclatante et fidèle.</li>
             <li><strong>Fini Satiné Lumineux :</strong> Apporte un éclat sophistiqué aux lèvres.</li>
             <li><strong>Confort & Hydratation :</strong> Formule enrichie en extraits naturels pour un confort durable. Protection SPF 15.</li>
             <li><strong>Écrin Couture :</strong> Design doré emblématique.</li>
           </ul>
           <h3>Conseils d''application :</h3>
           <p>Appliquer directement au raisin ou à l''aide d''un pinceau pour plus de précision.</p>
           <h3>Spécifications :</h3>
           <ul>
             <li><strong>Gamme :</strong> Rouge Pur Couture</li>
             <li><strong>Fini :</strong> Satiné</li>
             <li><strong>Teinte :</strong> Large gamme (Exemple: 01 Le Rouge - Rouge sang)</li>
             <li><strong>Contenance :</strong> 3.8g</li>
           </ul>
         </div>',
        'YSL Rouge Pur Couture Rouge à Lèvres', 'Yves Saint Laurent Rouge Pur Couture Rouge à Lèvres Satiné SPF 15',
        'Habillez vos lèvres de couleurs intenses et d''un fini satiné avec le rouge à lèvres iconique Rouge Pur Couture d''YSL.',
        'YSL, Rouge Pur Couture, rouge à lèvres, maquillage lèvres, satiné, couleur intense, SPF 15',
        'YSL Rouge Pur Couture Rouge à Lèvres | Satiné Iconique', 'Fini satiné, couleur intense',
        'ysl-rouge-pur-couture-lipstick', 'fr', 64)
on conflict (description_id) do nothing;

-- English Description (ID: 128)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (128, NOW(), NOW(),
        '<div>
           <h2>Yves Saint Laurent Rouge Pur Couture Lipstick</h2>
           <p>The iconic lipstick by Yves Saint Laurent. A range of unique shades inspired by YSL''s emblematic colors, in a couture golden case.</p>
           <h3>Benefits:</h3>
           <ul>
             <li><strong>Intense & Pure Colour:</strong> Rich pigments for radiant, true colour.</li>
             <li><strong>Luminous Satin Finish:</strong> Brings a sophisticated glow to the lips.</li>
             <li><strong>Comfort & Hydration:</strong> Formula enriched with natural extracts for lasting comfort. SPF 15 protection.</li>
             <li><strong>Couture Case:</strong> Emblematic golden design.</li>
           </ul>
           <h3>Application Tips:</h3>
           <p>Apply directly from the bullet or use a brush for more precision.</p>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Range:</strong> Rouge Pur Couture</li>
             <li><strong>Finish:</strong> Satin</li>
             <li><strong>Shade:</strong> Wide range (Example: 01 Le Rouge - Blood Red)</li>
             <li><strong>Content:</strong> 3.8g</li>
           </ul>
         </div>',
        'YSL Rouge Pur Couture Lipstick', 'Yves Saint Laurent Rouge Pur Couture Satin Lipstick SPF 15',
        'Dress your lips in intense colours and a satin finish with YSL''s iconic Rouge Pur Couture lipstick.',
        'YSL, Rouge Pur Couture, lipstick, lip makeup, satin, intense color, SPF 15',
        'YSL Rouge Pur Couture Lipstick | Iconic Satin', 'Satin finish, intense colour',
        'ysl-rouge-pur-couture-lipstick-en', 'en', 64)
on conflict (description_id) do nothing;

-- Product 65: Guerlain Mon Guerlain Eau de Parfum (Manuf: 8, Type: 7)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (65, NOW(), NOW(), true, NOW(), false, 10.5,
        false, null, true, false, 0.35, 7.0, -- Approx for 50ml
        'REF-GUER-FRAG-MONGUERLAIN65', 65, 8,
        '65f020632bc46470c104b76f', 7)
on conflict (product_id) do nothing;

-- French Description (ID: 129)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (129, NOW(), NOW(),
        '<div>
           <h2>Eau de Parfum Mon Guerlain</h2>
           <p>Mon Guerlain est un hommage à la féminité d’aujourd’hui : une féminité forte, libre et sensuelle, inspirée par Angelina Jolie. Un parfum oriental frais sublimé par des matières premières d''exception.</p>
           <h3>Composition Olfactive :</h3>
           <ul>
             <li><strong>Lavande Carla :</strong> Une variété d''exception cultivée en Provence.</li>
             <li><strong>Jasmin Sambac :</strong> Cueilli à l''aube, il offre au parfum toute sa finesse.</li>
             <li><strong>Vanille Tahitensis :</strong> Enveloppe la fragrance d''une sensualité addictive.</li>
             <li><strong>Santal Album :</strong> Bois précieux qui donne sa force et préserve le mystère.</li>
           </ul>
           <h3>Le Flacon "Quadrilobé" :</h3>
           <p>Créé en 1908, il s’est imposé comme un flacon iconique de la Maison. Épuré et graphique, il s’inspire d’un flacon d’alchimiste et doit son nom à son bouchon travaillé dans la masse pour obtenir une forme semblable à quatre lobes.</p>
           <h3>Spécifications :</h3>
           <ul>
             <li><strong>Type :</strong> Eau de Parfum</li>
             <li><strong>Famille Olfactive :</strong> Oriental Frais</li>
             <li><strong>Contenance :</strong> 50ml (Exemple)</li>
           </ul>
         </div>',
        'Guerlain Mon Guerlain Eau de Parfum', 'Guerlain Mon Guerlain Eau de Parfum - Parfum Oriental Femme',
        'Découvrez Mon Guerlain, l''eau de parfum orientale fraîche, hommage à la féminité moderne.',
        'Guerlain, Mon Guerlain, eau de parfum, parfum femme, oriental, lavande, vanille, sensuel',
        'Guerlain Mon Guerlain Eau de Parfum | Oriental Frais', 'Lavande Carla & Vanille Tahitensis',
        'guerlain-mon-guerlain-eau-de-parfum', 'fr', 65)
on conflict (description_id) do nothing;

-- English Description (ID: 130)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (130, NOW(), NOW(),
        '<div>
           <h2>Guerlain Mon Guerlain Eau de Parfum</h2>
           <p>Mon Guerlain is a tribute to today’s femininity: a strong, free and sensual femininity, inspired by Angelina Jolie. A fresh oriental fragrance enhanced by exceptional raw materials.</p>
           <h3>Olfactive Composition:</h3>
           <ul>
             <li><strong>Carla Lavender:</strong> An exceptional variety grown in Provence.</li>
             <li><strong>Sambac Jasmine:</strong> Picked at sunrise, it gives the fragrance all its finesse.</li>
             <li><strong>Tahitian Vanilla:</strong> Envelops the fragrance in an addictive sensuality.</li>
             <li><strong>Album Sandalwood:</strong> Precious wood that gives strength and preserves the mystery.</li>
           </ul>
           <h3>The "Quadrilobé" Bottle:</h3>
           <p>Created in 1908, it established itself as one of Guerlain’s iconic bottles. Simple and graphic, it echoes an alchemist’s bottle and owes its name to its stopper, crafted from one piece to obtain a shape resembling four lobes.</p>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Type:</strong> Eau de Parfum</li>
             <li><strong>Olfactive Family:</strong> Fresh Oriental</li>
             <li><strong>Volume:</strong> 50ml (Example)</li>
           </ul>
         </div>',
        'Guerlain Mon Guerlain Eau de Parfum', 'Guerlain Mon Guerlain Eau de Parfum - Women''s Oriental Perfume',
        'Discover Mon Guerlain, the fresh oriental eau de parfum, a tribute to modern femininity.',
        'Guerlain, Mon Guerlain, eau de parfum, women perfume, oriental, lavender, vanilla, sensual',
        'Guerlain Mon Guerlain Eau de Parfum | Fresh Oriental', 'Carla Lavender & Tahitian Vanilla',
        'guerlain-mon-guerlain-eau-de-parfum-en', 'en', 65)
on conflict (description_id) do nothing;

-- Product 66: Shiseido Vital Perfection Uplifting and Firming Cream (Manuf: 9, Type: 5)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (66, NOW(), NOW(), true, NOW(), false, 7.5,
        false, null, true, false, 0.28, 8.0, -- Jar dimensions
        'REF-SHIS-SKIN-VPUFCR66', 66, 9,
        '65f020632bc46470c104b76f', 5)
on conflict (product_id) do nothing;

-- French Description (ID: 131)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (131, NOW(), NOW(),
        '<div>
           <h2>Crème Lift Fermeté Vital Perfection Shiseido</h2>
           <p>Une crème anti-âge globale pour un effet lift et fermeté. Combat le relâchement cutané, les rides profondes et les taches brunes.</p>
           <h3>Technologie ReNeura++™ & Complexe KURENAI-TruLift :</h3>
           <ul>
             <li><strong>Effet Lift Visible :</strong> Renforce le potentiel d''auto-régénération de la peau pour un effet liftant.</li>
             <li><strong>Action Fermeté :</strong> Améliore la résilience et l''élasticité de la peau.</li>
             <li><strong>Correction Taches & Éclat :</strong> Complexe VP8 avec 4MSK pour un teint uniforme et lumineux.</li>
             <li><strong>Texture Riche et Soyeuse :</strong> Hydrate intensément et protège contre le dessèchement.</li>
           </ul>
           <h3>Résultats :</h3>
           <p>En 1 semaine, la peau est visiblement liftée*. En 4 semaines, la peau est plus ferme et les rides profondes sont réduites**.</p>
           <h3>Spécifications :</h3>
           <ul>
             <li><strong>Gamme :</strong> Vital Perfection</li>
             <li><strong>Type :</strong> Crème Lift Fermeté</li>
             <li><strong>Contenance :</strong> 50ml</li>
             <li><strong>Type de peau :</strong> Normale à Sèche</li>
           </ul>
           <p><small>*Auto-évaluation par 110 femmes.<br/>**Test clinique sur 35 femmes.</small></p>
         </div>',
        'Shiseido Vital Perfection Crème Lift Fermeté',
        'Shiseido Vital Perfection Uplifting and Firming Cream Crème Anti-Âge',
        'Liftez et raffermissez votre peau avec la crème Vital Perfection de Shiseido pour un visage visiblement plus jeune.',
        'Shiseido, Vital Perfection, crème lift, fermeté, anti-âge, soin visage, ReNeura',
        'Crème Shiseido Vital Perfection | Lift & Fermeté', 'Effet lift visible en 1 semaine',
        'shiseido-vital-perfection-uplifting-firming-cream', 'fr', 66)
on conflict (description_id) do nothing;

-- English Description (ID: 132)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (132, NOW(), NOW(),
        '<div>
           <h2>Shiseido Vital Perfection Uplifting and Firming Cream</h2>
           <p>A global anti-aging cream for a lifted and firmed look. Fights skin sagging, deep wrinkles, and dark spots.</p>
           <h3>ReNeura Technology++™ & KURENAI-TruLift Complex:</h3>
           <ul>
             <li><strong>Visible Lift Effect:</strong> Strengthens the skin''s self-regeneration potential for a lifting effect.</li>
             <li><strong>Firming Action:</strong> Improves skin resilience and elasticity.</li>
             <li><strong>Spot Correction & Radiance:</strong> VP8 complex with 4MSK for an even, luminous complexion.</li>
             <li><strong>Rich, Silky Texture:</strong> Intensely hydrates and protects against dryness.</li>
           </ul>
           <h3>Results:</h3>
           <p>In 1 week, skin is visibly lifted*. In 4 weeks, skin is firmer and deep wrinkles are reduced**.</p>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Range:</strong> Vital Perfection</li>
             <li><strong>Type:</strong> Uplifting and Firming Cream</li>
             <li><strong>Volume:</strong> 50ml</li>
             <li><strong>Skin Type:</strong> Normal to Dry</li>
           </ul>
           <p><small>*Self-assessment by 110 women.<br/>**Clinical test on 35 women.</small></p>
         </div>',
        'Shiseido Vital Perfection Uplifting and Firming Cream',
        'Shiseido Vital Perfection Uplifting and Firming Anti-Aging Cream',
        'Lift and firm your skin with Shiseido''s Vital Perfection cream for a visibly younger-looking face.',
        'Shiseido, Vital Perfection, lifting cream, firming, anti-aging, face care, ReNeura',
        'Cream Shiseido Vital Perfection | Lift & Firming', 'Visible lift effect in 1 week',
        'shiseido-vital-perfection-uplifting-firming-cream-en', 'en', 66)
on conflict (description_id) do nothing;

-- Product 67: NARS Sheer Glow Foundation (Manuf: 10, Type: 6)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (67, NOW(), NOW(), true, NOW(), false, 12.0,
        false, null, true, false, 0.12, 3.8,
        'REF-NARS-MAKE-SHEERGLOW67', 67, 10,
        '65f020632bc46470c104b76f', 6)
on conflict (product_id) do nothing;

-- French Description (ID: 133)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (133, NOW(), NOW(),
        '<div>
           <h2>Fond de Teint Sheer Glow NARS</h2>
           <p>Un fond de teint éclatant à la couvrance modulable qui unifie le teint pour un fini naturel et lumineux. Améliore la texture et l''éclat de la peau au fil des utilisations.</p>
           <h3>Bénéfices :</h3>
           <ul>
             <li><strong>Éclat Naturel :</strong> Illumine la peau instantanément et durablement.</li>
             <li><strong>Couvrance Modulable :</strong> Permet d''obtenir un résultat sur mesure, de léger à moyen.</li>
             <li><strong>Améliore la Peau :</strong> Formule soin avec le Complexe Éclat NARS pour améliorer la texture et l''éclat.</li>
             <li><strong>Hydratation :</strong> Convient aux peaux normales à sèches.</li>
           </ul>
           <h3>Conseils d''application :</h3>
           <p>Appliquer du bout des doigts pour un fini naturel. Peut être appliqué au pinceau ou à l''éponge pour plus de couvrance.</p>
           <h3>Spécifications :</h3>
           <ul>
             <li><strong>Gamme :</strong> Sheer Glow Foundation</li>
             <li><strong>Fini :</strong> Naturel Lumineux</li>
             <li><strong>Contenance :</strong> 30ml</li>
             <li><strong>Teinte :</strong> Large gamme (Exemple: Fiji - Clair à moyen, tons jaunes)</li>
           </ul>
         </div>',
        'NARS Sheer Glow Foundation', 'NARS Sheer Glow Foundation Fond de Teint Éclat Naturel',
        'Obtenez un teint naturellement lumineux avec le fond de teint Sheer Glow de NARS.',
        'NARS, Sheer Glow, fond de teint, maquillage teint, éclat naturel, couvrance modulable',
        'Fond de Teint NARS Sheer Glow | Éclat Naturel', 'Fini naturel et lumineux', 'nars-sheer-glow-foundation', 'fr',
        67)
on conflict (description_id) do nothing;

-- English Description (ID: 134)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (134, NOW(), NOW(),
        '<div>
           <h2>NARS Sheer Glow Foundation</h2>
           <p>A glowing foundation with buildable coverage that evens the complexion for a natural, luminous finish. Improves skin texture and radiance with daily use.</p>
           <h3>Benefits:</h3>
           <ul>
             <li><strong>Natural Glow:</strong> Brightens skin instantly and over time.</li>
             <li><strong>Buildable Coverage:</strong> Allows for a customized result, from light to medium.</li>
             <li><strong>Improves Skin:</strong> Skincare formula with NARS Complexion Brightening Complex to improve texture and radiance.</li>
             <li><strong>Hydration:</strong> Suitable for normal to dry skin types.</li>
           </ul>
           <h3>Application Tips:</h3>
           <p>Apply with fingertips for a natural finish. Can be applied with a brush or sponge for more coverage.</p>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Range:</strong> Sheer Glow Foundation</li>
             <li><strong>Finish:</strong> Natural Luminous</li>
             <li><strong>Volume:</strong> 30ml</li>
             <li><strong>Shade:</strong> Wide range (Example: Fiji - Light to medium with yellow undertones)</li>
           </ul>
         </div>',
        'NARS Sheer Glow Foundation', 'NARS Sheer Glow Natural Radiant Foundation',
        'Achieve a naturally luminous complexion with NARS Sheer Glow foundation.',
        'NARS, Sheer Glow, foundation, face makeup, natural glow, buildable coverage',
        'Foundation NARS Sheer Glow | Natural Glow', 'Natural luminous finish', 'nars-sheer-glow-foundation-en', 'en',
        67)
on conflict (description_id) do nothing;

-- Product 68: La Roche-Posay Effaclar Duo (+) (Manuf: 11, Type: 5)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (68, NOW(), NOW(), true, NOW(), false, 14.5,
        false, null, true, false, 0.06, 3.5, -- Approx for 40ml tube
        'REF-LRP-SKIN-EFFADUO68', 68, 11,
        '65f020632bc46470c104b76f', 5)
on conflict (product_id) do nothing;

-- French Description (ID: 135)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (135, NOW(), NOW(),
        '<div>
           <h2>Soin Correcteur Anti-Imperfections Effaclar Duo (+) La Roche-Posay</h2>
           <p>Soin complet qui favorise la résorption rapide des boutons existants, prévient leur réapparition et limite le risque de marques. Efficacité dès 12h*.</p>
           <h3>Bénéfices :</h3>
           <ul>
             <li><strong>Anti-Imperfections :</strong> Niacinamide et LHA pour désincruster les pores et corriger les imperfections sévères.</li>
             <li><strong>Anti-Marques :</strong> Procerad™ (céramide breveté) pour prévenir les marques rouges et brunes.</li>
             <li><strong>Anti-Récidive :</strong> Aqua Posae Filiformis pour rééquilibrer le microbiome cutané.</li>
             <li><strong>Texture Gel-Crème :</strong> Hydratant et matifiant, excellente base de maquillage.</li>
           </ul>
           <h3>Conseils d''utilisation :</h3>
           <p>Appliquer matin et/ou soir sur l''ensemble du visage préalablement nettoyé avec Effaclar Gel Moussant.</p>
           <h3>Spécifications :</h3>
           <ul>
             <li><strong>Gamme :</strong> Effaclar</li>
             <li><strong>Type de peau :</strong> Peaux grasses à tendance acnéique, imperfections sévères.</li>
             <li><strong>Contenance :</strong> 40ml</li>
             <li><strong>Formule :</strong> Hypoallergénique, non comédogène.</li>
           </ul>
           <p><small>*Test instrumental, 43 sujets.</small></p>
         </div>',
        'La Roche-Posay Effaclar Duo (+) Soin Anti-Imperfections',
        'La Roche-Posay Effaclar Duo (+) Soin Correcteur Anti-Imperfections Anti-Marques',
        'Corrigez les imperfections sévères et prévenez les marques avec Effaclar Duo (+) de La Roche-Posay.',
        'La Roche-Posay, Effaclar Duo, soin acné, anti-imperfections, anti-marques, peau grasse, niacinamide',
        'Soin La Roche-Posay Effaclar Duo (+) | Anti-Imperfections', 'Efficacité anti-marques',
        'la-roche-posay-effaclar-duo-plus', 'fr', 68)
on conflict (description_id) do nothing;

-- English Description (ID: 136)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (136, NOW(), NOW(),
        '<div>
           <h2>La Roche-Posay Effaclar Duo (+) Anti-Imperfection Treatment</h2>
           <p>Complete care that promotes the rapid resorption of existing spots, prevents their reappearance, and limits the risk of marks. Efficacy from 12h*.</p>
           <h3>Benefits:</h3>
           <ul>
             <li><strong>Anti-Imperfections:</strong> Niacinamide and LHA to unclog pores and correct severe imperfections.</li>
             <li><strong>Anti-Marks:</strong> Procerad™ (patented ceramide) to prevent red and brown marks.</li>
             <li><strong>Anti-Recurrence:</strong> Aqua Posae Filiformis to rebalance the skin microbiome.</li>
             <li><strong>Gel-Cream Texture:</strong> Moisturizing and mattifying, excellent makeup base.</li>
           </ul>
           <h3>How to Use:</h3>
           <p>Apply morning and/or evening to the entire face after cleansing with Effaclar Purifying Foaming Gel.</p>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Range:</strong> Effaclar</li>
             <li><strong>Skin Type:</strong> Oily acne-prone skin, severe imperfections.</li>
             <li><strong>Volume:</strong> 40ml</li>
             <li><strong>Formula:</strong> Hypoallergenic, non-comedogenic.</li>
           </ul>
           <p><small>*Instrumental test, 43 subjects.</small></p>
         </div>',
        'La Roche-Posay Effaclar Duo (+) Anti-Imperfection Treatment',
        'La Roche-Posay Effaclar Duo (+) Corrective Unclogging Anti-Imperfection Anti-Mark Care',
        'Correct severe imperfections and prevent marks with La Roche-Posay Effaclar Duo (+).',
        'La Roche-Posay, Effaclar Duo, acne treatment, anti-imperfection, anti-mark, oily skin, niacinamide',
        'Treatment La Roche-Posay Effaclar Duo (+) | Anti-Imperfection', 'Anti-mark efficacy',
        'la-roche-posay-effaclar-duo-plus-en', 'en', 68)
on conflict (description_id) do nothing;

-- Product 69: Kérastase Chronologiste Huile de Parfum (Manuf: 12, Type: 8)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (69, NOW(), NOW(), true, NOW(), false, 15.0,
        false, null, true, false, 0.35, 6.0,
        'REF-KERA-HAIR-CHRONOHUILE69', 69, 12,
        '65f020632bc46470c104b76f', 8)
on conflict (product_id) do nothing;

-- French Description (ID: 137)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (137, NOW(), NOW(),
        '<div>
           <h2>Huile de Parfum Chronologiste Kérastase</h2>
           <p>Une huile parfumée sublimatrice pour une nutrition intense, une protection thermique et un parfumage délicat des cheveux.</p>
           <h3>Bénéfices :</h3>
           <ul>
             <li><strong>Nutrition & Douceur :</strong> Nourrit la fibre capillaire pour une douceur et une souplesse incomparables.</li>
             <li><strong>Brillance Ultime :</strong> Sublime l''éclat naturel des cheveux.</li>
             <li><strong>Parfum Signature :</strong> Créé par le maître parfumeur Alberto Morillas, aux notes de Rose Thé, Bois Blonds et Musc.</li>
             <li><strong>Protection Thermique :</strong> Protège de la chaleur jusqu''à 230°C.</li>
             <li><strong>Action Anti-Pollution :</strong> Aide à protéger les cheveux des agressions extérieures.</li>
           </ul>
           <h3>Conseils d''utilisation :</h3>
           <p>Appliquer 1 à 2 pressions sur cheveux secs ou humides, sans rincer. Répartir sur les longueurs et pointes.</p>
           <h3>Spécifications :</h3>
           <ul>
             <li><strong>Gamme :</strong> Chronologiste</li>
             <li><strong>Type de cheveux :</strong> Tous types</li>
             <li><strong>Contenance :</strong> 100ml</li>
           </ul>
         </div>',
        'Kérastase Chronologiste Huile de Parfum', 'Kérastase Chronologiste Huile de Parfum Fragrance-in-Oil',
        'Nourrissez et parfumez vos cheveux avec l''Huile de Parfum Chronologiste de Kérastase.',
        'Kérastase, Chronologiste, huile cheveux, huile parfumée, nutrition, brillance, parfum cheveux',
        'Huile Kérastase Chronologiste | Parfum & Nutrition', 'Parfum signature & Nutrition',
        'kerastase-chronologiste-huile-de-parfum', 'fr', 69)
on conflict (description_id) do nothing;

-- English Description (ID: 138)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (138, NOW(), NOW(),
        '<div>
           <h2>Kérastase Chronologiste Huile de Parfum Fragrance-in-Oil</h2>
           <p>A sublimating fragrant oil for intense nutrition, heat protection, and delicate hair fragrancing.</p>
           <h3>Benefits:</h3>
           <ul>
             <li><strong>Nutrition & Softness:</strong> Nourishes the hair fiber for incomparable softness and suppleness.</li>
             <li><strong>Ultimate Shine:</strong> Enhances the hair''s natural radiance.</li>
             <li><strong>Signature Fragrance:</strong> Created by master perfumer Alberto Morillas, with notes of Tea Rose, Light Woods, and Musk.</li>
             <li><strong>Heat Protection:</strong> Protects from heat up to 230°C.</li>
             <li><strong>Anti-Pollution Action:</strong> Helps protect hair from external aggressors.</li>
           </ul>
           <h3>How to Use:</h3>
           <p>Apply 1 to 2 pumps on dry or damp hair, without rinsing. Distribute through lengths and ends.</p>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Range:</strong> Chronologiste</li>
             <li><strong>Hair Type:</strong> All types</li>
             <li><strong>Volume:</strong> 100ml</li>
           </ul>
         </div>',
        'Kérastase Chronologiste Huile de Parfum Fragrance-in-Oil',
        'Kérastase Chronologiste Huile de Parfum Fragrance-in-Oil',
        'Nourish and fragrance your hair with Kérastase Chronologiste Huile de Parfum.',
        'Kérastase, Chronologiste, hair oil, fragrance oil, nutrition, shine, hair perfume',
        'Kérastase Chronologiste Oil | Fragrance & Nutrition', 'Signature fragrance & Nutrition',
        'kerastase-chronologiste-huile-de-parfum-en', 'en', 69)
on conflict (description_id) do nothing;

-- Product 70: YSL Mon Paris Eau de Parfum (Manuf: 7, Type: 7)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (70, NOW(), NOW(), true, NOW(), false, 9.0,
        false, null, true, false, 0.3, 8.0, -- Approx for 50ml
        'REF-YSL-FRAG-MONPARIS70', 70, 7,
        '65f020632bc46470c104b76f', 7)
on conflict (product_id) do nothing;

-- French Description (ID: 139)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (139, NOW(), NOW(),
        '<div>
           <h2>Eau de Parfum Mon Paris Yves Saint Laurent</h2>
           <p>Le nouveau chypre blanc vertigineux. Le parfum des amants terribles pour vivre l’intensité de l’amour fou.</p>
           <h3>Composition Olfactive :</h3>
           <p>La sensualité hypnotique du Datura, fleur qui ne révèle son parfum qu''à la nuit tombée, associée à l''intensité du Patchouli et la caresse des Muscs Blancs.</p>
           <ul>
             <li><strong>Note de Tête :</strong> Fruits Rouges, Bergamote</li>
             <li><strong>Note de Cœur :</strong> Fleur de Datura, Jasmin Sambac</li>
             <li><strong>Note de Fond :</strong> Patchouli, Muscs Blancs</li>
           </ul>
           <h3>Le Flacon :</h3>
           <p>Un flacon multi-facetté, paré d''une lavallière en soie noire et du Cassandre YSL doré.</p>
           <h3>Spécifications :</h3>
           <ul>
             <li><strong>Type :</strong> Eau de Parfum</li>
             <li><strong>Famille Olfactive :</strong> Chypré Fruité</li>
             <li><strong>Contenance :</strong> 50ml (Exemple)</li>
           </ul>
         </div>',
        'YSL Mon Paris Eau de Parfum', 'Yves Saint Laurent Mon Paris Eau de Parfum - Parfum Chypré Fruité Femme',
        'Vivez l''amour fou avec Mon Paris d''YSL, l''eau de parfum chyprée fruitée au Datura hypnotique.',
        'YSL, Mon Paris, eau de parfum, parfum femme, chypré, fruité, datura, patchouli, amour',
        'YSL Mon Paris Eau de Parfum | Chypré Vertigineux', 'Datura hypnotique & Patchouli',
        'ysl-mon-paris-eau-de-parfum', 'fr', 70)
on conflict (description_id) do nothing;

-- English Description (ID: 140)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (140, NOW(), NOW(),
        '<div>
           <h2>Yves Saint Laurent Mon Paris Eau de Parfum</h2>
           <p>The new vertiginous white chypre. The fragrance of passionate lovers living the intensity of mad love.</p>
           <h3>Olfactive Composition:</h3>
           <p>The hypnotic sensuality of Datura flower, which only reveals its fragrance at nightfall, combined with the intensity of Patchouli and the caress of White Musks.</p>
           <ul>
             <li><strong>Top Note:</strong> Red Berries, Bergamot</li>
             <li><strong>Heart Note:</strong> Datura Flower, Sambac Jasmine</li>
             <li><strong>Base Note:</strong> Patchouli, White Musks</li>
           </ul>
           <h3>The Bottle:</h3>
           <p>A multi-faceted bottle, adorned with a black silk lavallière and the golden YSL Cassandre.</p>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Type:</strong> Eau de Parfum</li>
             <li><strong>Olfactive Family:</strong> Fruity Chypre</li>
             <li><strong>Volume:</strong> 50ml (Example)</li>
           </ul>
         </div>',
        'YSL Mon Paris Eau de Parfum', 'Yves Saint Laurent Mon Paris Eau de Parfum - Women''s Fruity Chypre Perfume',
        'Experience mad love with YSL''s Mon Paris, the fruity chypre eau de parfum with hypnotic Datura.',
        'YSL, Mon Paris, eau de parfum, women perfume, chypre, fruity, datura, patchouli, love',
        'YSL Mon Paris Eau de Parfum | Vertiginous Chypre', 'Hypnotic Datura & Patchouli',
        'ysl-mon-paris-eau-de-parfum-en', 'en', 70)
on conflict (description_id) do nothing;

-- Product 71: Guerlain Orchidée Impériale The Cream (Manuf: 8, Type: 5)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (71, NOW(), NOW(), true, NOW(), false, 8.0,
        false, null, true, false, 0.4, 8.5, -- Jar dimensions, heavier glass
        'REF-GUER-SKIN-OICREAM71', 71, 8,
        '65f020632bc46470c104b76f', 5)
on conflict (product_id) do nothing;

-- French Description (ID: 141)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (141, NOW(), NOW(),
        '<div>
           <h2>La Crème Orchidée Impériale Guerlain</h2>
           <p>Le chef-d’œuvre de longévité Guerlain. Un soin complet d''exception qui cible les signes visibles de l''âge pour une peau régénérée et revitalisée.</p>
           <h3>Technologie Orchid Totum™ Moléculaire :</h3>
           <ul>
             <li><strong>Pouvoir Régénérant :</strong> Concentre le pouvoir de millions de molécules d''orchidée pour aider à réguler la respiration cellulaire et favoriser la régénération.</li>
             <li><strong>Action Anti-Âge Globale :</strong> Agit sur les rides, la fermeté, l''élasticité, l''homogénéité et l''éclat.</li>
             <li><strong>Texture Fine et Fondante :</strong> Offre un confort immédiat et durable.</li>
             <li><strong>Éco-Conception :</strong> Pot en verre allégé et recharge disponible.</li>
           </ul>
           <h3>Résultats :</h3>
           <p>La peau est visiblement plus jeune, plus dense, plus ferme. Le teint est unifié et éclatant.</p>
           <h3>Spécifications :</h3>
           <ul>
             <li><strong>Gamme :</strong> Orchidée Impériale</li>
             <li><strong>Type :</strong> Crème Anti-Âge Global</li>
             <li><strong>Contenance :</strong> 50ml</li>
           </ul>
         </div>',
        'Guerlain Orchidée Impériale La Crème',
        'Guerlain Orchidée Impériale La Crème Soin Complet d''Exception Anti-Âge',
        'Découvrez La Crème Orchidée Impériale de Guerlain, le soin anti-âge global d''exception.',
        'Guerlain, Orchidée Impériale, crème, anti-âge global, régénération, soin visage, luxe',
        'Crème Guerlain Orchidée Impériale | Anti-Âge Global', 'Technologie Orchid Totum™',
        'guerlain-orchidee-imperiale-the-cream', 'fr', 71)
on conflict (description_id) do nothing;

-- English Description (ID: 142)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (142, NOW(), NOW(),
        '<div>
           <h2>Guerlain Orchidée Impériale The Cream</h2>
           <p>Guerlain''s masterpiece of longevity. An exceptional complete care product that targets the visible signs of aging for regenerated and revitalized skin.</p>
           <h3>Orchid Totum™ Molecular Technology:</h3>
           <ul>
             <li><strong>Regenerating Power:</strong> Concentrates the power of millions of orchid molecules to help regulate cell respiration and promote regeneration.</li>
             <li><strong>Global Anti-Aging Action:</strong> Acts on wrinkles, firmness, elasticity, evenness, and radiance.</li>
             <li><strong>Fine, Melting Texture:</strong> Offers immediate and lasting comfort.</li>
             <li><strong>Eco-Design:</strong> Lighter glass jar and refill available.</li>
           </ul>
           <h3>Results:</h3>
           <p>Skin is visibly younger, denser, firmer. The complexion is even and radiant.</p>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Range:</strong> Orchidée Impériale</li>
             <li><strong>Type:</strong> Global Anti-Aging Cream</li>
             <li><strong>Volume:</strong> 50ml</li>
           </ul>
         </div>',
        'Guerlain Orchidée Impériale The Cream',
        'Guerlain Orchidée Impériale The Cream Exceptional Complete Care Anti-Aging',
        'Discover Guerlain Orchidée Impériale The Cream, the exceptional global anti-aging care.',
        'Guerlain, Orchidée Impériale, cream, global anti-aging, regeneration, face care, luxury',
        'Cream Guerlain Orchidée Impériale | Global Anti-Aging', 'Orchid Totum™ Technology',
        'guerlain-orchidee-imperiale-the-cream-en', 'en', 71)
on conflict (description_id) do nothing;

-- Product 72: Shiseido Minimalist WhippedPowder Blush (Manuf: 9, Type: 6)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (72, NOW(), NOW(), true, NOW(), false, 4.5,
        false, null, true, false, 0.04, 4.5, -- Small pot dimensions
        'REF-SHIS-MAKE-MINBLUSH72', 72, 9,
        '65f020632bc46470c104b76f', 6)
on conflict (product_id) do nothing;

-- French Description (ID: 143)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (143, NOW(), NOW(),
        '<div>
           <h2>Blush Minimalist WhippedPowder Shiseido</h2>
           <p>Un blush crème-poudre aérien qui se transforme en une poudre impalpable au fini mat doux et longue tenue.</p>
           <h3>Caractéristiques :</h3>
           <ul>
             <li><strong>Texture Unique :</strong> Mousse fouettée qui fond sur la peau.</li>
             <li><strong>Fini Mat Doux :</strong> Couleur naturelle et diffusée.</li>
             <li><strong>Longue Tenue :</strong> Reste en place pendant 8 heures*.</li>
             <li><strong>Application Facile :</strong> S''estompe sans effort du bout des doigts ou au pinceau.</li>
           </ul>
           <h3>Technologie AirFusion :</h3>
           <p>Des micro-bulles d''air pour une sensation de légèreté unique.</p>
           <h3>Spécifications :</h3>
           <ul>
             <li><strong>Gamme :</strong> Makeup</li>
             <li><strong>Type :</strong> Blush Crème-Poudre</li>
             <li><strong>Teinte :</strong> Plusieurs teintes (Exemple: 04 Eiko - Rose Corail)</li>
             <li><strong>Contenance :</strong> 5g</li>
           </ul>
           <p><small>*Test instrumental sur 31 volontaires.</small></p>
         </div>',
        'Shiseido Minimalist WhippedPowder Blush', 'Shiseido Minimalist WhippedPowder Blush Crème-Poudre Fini Mat',
        'Découvrez le blush aérien Minimalist WhippedPowder de Shiseido pour une couleur mate naturelle.',
        'Shiseido, Minimalist WhippedPowder Blush, blush, fard à joues, crème-poudre, mat, maquillage teint',
        'Blush Shiseido Minimalist | Fini Mat Aérien', 'Texture crème-poudre unique',
        'shiseido-minimalist-whippedpowder-blush', 'fr', 72)
on conflict (description_id) do nothing;

-- English Description (ID: 144)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (144, NOW(), NOW(),
        '<div>
           <h2>Shiseido Minimalist WhippedPowder Blush</h2>
           <p>An airy cream-powder blush that transforms into an impalpable powder with a soft matte finish and long-lasting wear.</p>
           <h3>Features:</h3>
           <ul>
             <li><strong>Unique Texture:</strong> Whipped mousse that melts onto the skin.</li>
             <li><strong>Soft Matte Finish:</strong> Natural, diffused color.</li>
             <li><strong>Long-Wearing:</strong> Stays put for 8 hours*.</li>
             <li><strong>Easy Application:</strong> Blends effortlessly with fingertips or a brush.</li>
           </ul>
           <h3>AirFusion Technology:</h3>
           <p>Micro-bubbles of air for a unique lightweight sensation.</p>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Range:</strong> Makeup</li>
             <li><strong>Type:</strong> Cream-Powder Blush</li>
             <li><strong>Shade:</strong> Several shades (Example: 04 Eiko - Coral Pink)</li>
             <li><strong>Content:</strong> 5g</li>
           </ul>
           <p><small>*Instrumental test on 31 volunteers.</small></p>
         </div>',
        'Shiseido Minimalist WhippedPowder Blush',
        'Shiseido Minimalist WhippedPowder Cream-to-Powder Matte Finish Blush',
        'Discover Shiseido''s airy Minimalist WhippedPowder blush for a natural matte color.',
        'Shiseido, Minimalist WhippedPowder Blush, blush, cheek color, cream-to-powder, matte, face makeup',
        'Blush Shiseido Minimalist | Airy Matte Finish', 'Unique cream-to-powder texture',
        'shiseido-minimalist-whippedpowder-blush-en', 'en', 72)
on conflict (description_id) do nothing;

-- Product 73: NARS Climax Mascara (Manuf: 10, Type: 6)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (73, NOW(), NOW(), true, NOW(), false, 13.0,
        false, null, true, false, 0.05, 2.0,
        'REF-NARS-MAKE-CLIMAXMASC73', 73, 10,
        '65f020632bc46470c104b76f', 6)
on conflict (product_id) do nothing;

-- French Description (ID: 145)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (145, NOW(), NOW(),
        '<div>
           <h2>Mascara Climax NARS</h2>
           <p>Atteignez le sommet du volume. Un mascara innovant pour un volume explosif et une légèreté incroyable, sans paquet ni bavure.</p>
           <h3>Bénéfices :</h3>
           <ul>
             <li><strong>Volume Explosif Modulable :</strong> Brosse XXL exclusive aux poils nervurés pour charger les cils instantanément.</li>
             <li><strong>Légèreté Maximale :</strong> Formule fouettée et crémeuse pour des cils souples.</li>
             <li><strong>Sans Paquet, Sans Bavure :</strong> Application fluide et résultat impeccable.</li>
             <li><strong>Noir Intense :</strong> Pigments noirs saturés.</li>
           </ul>
           <h3>Conseils d''application :</h3>
           <p>Appliquer de la racine aux pointes en effectuant un mouvement de zigzag. Superposer les couches pour un volume plus intense.</p>
           <h3>Spécifications :</h3>
           <ul>
             <li><strong>Gamme :</strong> Climax Mascara</li>
             <li><strong>Effet :</strong> Volume Explosif</li>
             <li><strong>Teinte :</strong> Explicit Black</li>
             <li><strong>Contenance :</strong> 6g</li>
           </ul>
         </div>',
        'NARS Climax Mascara', 'NARS Climax Mascara Volume Explosif Léger',
        'Obtenez un volume explosif sans paquet avec le mascara Climax de NARS.',
        'NARS, Climax, mascara, maquillage yeux, volume, noir intense', 'Mascara NARS Climax | Volume Explosif',
        'Volume explosif, légèreté incroyable', 'nars-climax-mascara', 'fr', 73)
on conflict (description_id) do nothing;

-- English Description (ID: 146)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (146, NOW(), NOW(),
        '<div>
           <h2>NARS Climax Mascara</h2>
           <p>Reach the peak of volume. An innovative mascara for explosive volume and incredible lightness, without clumping or smudging.</p>
           <h3>Benefits:</h3>
           <ul>
             <li><strong>Explosive Buildable Volume:</strong> Exclusive XXL brush with ribbed bristles to load lashes instantly.</li>
             <li><strong>Maximum Lightness:</strong> Whipped, creamy formula for flexible lashes.</li>
             <li><strong>No Clumps, No Smudges:</strong> Smooth application and flawless result.</li>
             <li><strong>Intense Black:</strong> Saturated black pigments.</li>
           </ul>
           <h3>Application Tips:</h3>
           <p>Apply from root to tip using a zigzag motion. Layer for more intense volume.</p>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Range:</strong> Climax Mascara</li>
             <li><strong>Effect:</strong> Explosive Volume</li>
             <li><strong>Shade:</strong> Explicit Black</li>
             <li><strong>Content:</strong> 6g</li>
           </ul>
         </div>',
        'NARS Climax Mascara', 'NARS Climax Lightweight Explosive Volume Mascara',
        'Achieve explosive, clump-free volume with NARS Climax mascara.',
        'NARS, Climax, mascara, eye makeup, volume, intense black', 'Mascara NARS Climax | Explosive Volume',
        'Explosive volume, incredible lightness', 'nars-climax-mascara-en', 'en', 73)
on conflict (description_id) do nothing;

-- Product 74: La Roche-Posay Toleriane Sensitive Crème (Manuf: 11, Type: 5)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (74, NOW(), NOW(), true, NOW(), false, 13.0,
        false, null, true, false, 0.06, 3.0, -- Approx for 40ml tube
        'REF-LRP-SKIN-TOLSENSCR74', 74, 11,
        '65f020632bc46470c104b76f', 5)
on conflict (product_id) do nothing;

-- French Description (ID: 147)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (147, NOW(), NOW(),
        '<div>
           <h2>Crème Hydratante Apaisante Toleriane Sensitive La Roche-Posay</h2>
           <p>Le soin hydratant prébiotique qui apaise, protège et hydrate les peaux sensibles. Réduit la sensibilité cutanée.</p>
           <h3>Bénéfices :</h3>
           <ul>
             <li><strong>Hydratation 48h :</strong> Glycérine pour une hydratation longue durée.</li>
             <li><strong>Apaise Immédiatement :</strong> Vitamine B3 (Niacinamide) pour calmer les rougeurs et sensations d''inconfort.</li>
             <li><strong>Répare et Protège la Barrière Cutanée :</strong> Céramide III et Eau Thermale de La Roche-Posay.</li>
             <li><strong>Action Prébiotique :</strong> Rééquilibre le microbiome cutané pour renforcer les défenses naturelles.</li>
           </ul>
           <h3>Haute Tolérance :</h3>
           <p>0% parfum, 0% alcool. Testé sur peaux ultra-sensibles. Convient aux bébés.</p>
           <h3>Spécifications :</h3>
           <ul>
             <li><strong>Gamme :</strong> Toleriane</li>
             <li><strong>Type de peau :</strong> Peaux sensibles à très sensibles, sujettes aux rougeurs et inconforts.</li>
             <li><strong>Contenance :</strong> 40ml</li>
           </ul>
         </div>',
        'La Roche-Posay Toleriane Sensitive Crème',
        'La Roche-Posay Toleriane Sensitive Crème Hydratante Apaisante Prébiotique',
        'Hydratez et apaisez votre peau sensible avec la crème prébiotique Toleriane Sensitive de La Roche-Posay.',
        'La Roche-Posay, Toleriane Sensitive, crème hydratante, apaisant, peau sensible, prébiotique, niacinamide',
        'Crème La Roche-Posay Toleriane Sensitive | Peau Sensible', 'Hydratant prébiotique apaisant',
        'la-roche-posay-toleriane-sensitive-creme', 'fr', 74)
on conflict (description_id) do nothing;

-- English Description (ID: 148)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (148, NOW(), NOW(),
        '<div>
           <h2>La Roche-Posay Toleriane Sensitive Soothing Moisturiser</h2>
           <p>The prebiotic moisturising care that soothes, protects, and hydrates sensitive skin. Reduces skin sensitivity.</p>
           <h3>Benefits:</h3>
           <ul>
             <li><strong>48h Hydration:</strong> Glycerin for long-lasting hydration.</li>
             <li><strong>Immediately Soothes:</strong> Vitamin B3 (Niacinamide) to calm redness and discomfort.</li>
             <li><strong>Repairs and Protects Skin Barrier:</strong> Ceramide III and La Roche-Posay Thermal Spring Water.</li>
             <li><strong>Prebiotic Action:</strong> Rebalances the skin microbiome to strengthen natural defenses.</li>
           </ul>
           <h3>High Tolerance:</h3>
           <p>0% fragrance, 0% alcohol. Tested on ultra-sensitive skin. Suitable for babies.</p>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Range:</strong> Toleriane</li>
             <li><strong>Skin Type:</strong> Sensitive to very sensitive skin, prone to redness and discomfort.</li>
             <li><strong>Volume:</strong> 40ml</li>
           </ul>
         </div>',
        'La Roche-Posay Toleriane Sensitive Cream', 'La Roche-Posay Toleriane Sensitive Prebiotic Soothing Moisturiser',
        'Hydrate and soothe your sensitive skin with La Roche-Posay Toleriane Sensitive prebiotic cream.',
        'La Roche-Posay, Toleriane Sensitive, moisturiser, soothing, sensitive skin, prebiotic, niacinamide',
        'Cream La Roche-Posay Toleriane Sensitive | Sensitive Skin', 'Soothing prebiotic moisturiser',
        'la-roche-posay-toleriane-sensitive-creme-en', 'en', 74)
on conflict (description_id) do nothing;

-- Product 75: Kérastase Resistance Ciment Thermique (Manuf: 12, Type: 8)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (75, NOW(), NOW(), true, NOW(), false, 19.0,
        false, null, true, false, 0.18, 4.5, -- Approx for 150ml tube
        'REF-KERA-HAIR-CIMENTHERM75', 75, 12,
        '65f020632bc46470c104b76f', 8)
on conflict (product_id) do nothing;

-- French Description (ID: 149)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (149, NOW(), NOW(),
        '<div>
           <h2>Lait Ciment Thermique Resistance Kérastase</h2>
           <p>Lait reconstructeur thermo-protecteur pour cheveux affaiblis et abîmés. Protège de la chaleur jusqu''à 180°C et facilite le brushing.</p>
           <h3>Bénéfices :</h3>
           <ul>
             <li><strong>Thermo-Protection :</strong> Protège la fibre de la chaleur du sèche-cheveux et des outils de coiffage.</li>
             <li><strong>Action Reconstructrice :</strong> Complexe Vita-Ciment® (Pro-Kératine + Céramide) pour renforcer la fibre de l''intérieur.</li>
             <li><strong>Facilite le Brushing :</strong> Réduit le temps de séchage et rend les cheveux plus faciles à coiffer.</li>
             <li><strong>Anti-Casse & Anti-Fourches :</strong> Renforce la résistance des cheveux.</li>
           </ul>
           <h3>Conseils d''utilisation :</h3>
           <p>Appliquer une noisette sur cheveux lavés et essorés. Répartir sur les longueurs et pointes. Procéder au brushing. Sans rinçage.</p>
           <h3>Spécifications :</h3>
           <ul>
             <li><strong>Gamme :</strong> Resistance</li>
             <li><strong>Type de cheveux :</strong> Affaiblis, Abîmés, Cassants</li>
             <li><strong>Contenance :</strong> 150ml</li>
           </ul>
         </div>',
        'Kérastase Resistance Ciment Thermique',
        'Kérastase Resistance Ciment Thermique Lait Reconstructeur Thermo-Protecteur',
        'Protégez et reconstruisez vos cheveux abîmés avec le Ciment Thermique de Kérastase.',
        'Kérastase, Resistance, Ciment Thermique, thermo-protecteur, brushing, cheveux abîmés, anti-casse',
        'Soin Kérastase Ciment Thermique | Thermo-Protecteur', 'Protège jusqu''à 180°C',
        'kerastase-resistance-ciment-thermique', 'fr', 75)
on conflict (description_id) do nothing;

-- English Description (ID: 150)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (150, NOW(), NOW(),
        '<div>
           <h2>Kérastase Resistance Ciment Thermique Blow-Dry Primer</h2>
           <p>Resurfacing strengthening blow-dry milk for weakened and damaged hair. Protects from heat up to 180°C and facilitates blow-drying.</p>
           <h3>Benefits:</h3>
           <ul>
             <li><strong>Heat Protection:</strong> Protects the fiber from the heat of hairdryers and styling tools.</li>
             <li><strong>Reconstructing Action:</strong> Vita-Ciment® Complex (Pro-Keratin + Ceramide) to strengthen the fiber from within.</li>
             <li><strong>Easier Blow-Dry:</strong> Reduces drying time and makes hair easier to style.</li>
             <li><strong>Anti-Breakage & Anti-Split Ends:</strong> Reinforces hair resistance.</li>
           </ul>
           <h3>How to Use:</h3>
           <p>Apply a chestnut-sized amount to washed and towel-dried hair. Distribute through lengths and ends. Proceed to blow-dry. Leave-in.</p>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Range:</strong> Resistance</li>
             <li><strong>Hair Type:</strong> Weakened, Damaged, Brittle</li>
             <li><strong>Volume:</strong> 150ml</li>
           </ul>
         </div>',
        'Kérastase Resistance Ciment Thermique',
        'Kérastase Resistance Ciment Thermique Resurfacing Strengthening Blow-Dry Milk',
        'Protect and reconstruct your damaged hair with Kérastase Ciment Thermique.',
        'Kérastase, Resistance, Ciment Thermique, heat protectant, blow-dry, damaged hair, anti-breakage',
        'Treatment Kérastase Ciment Thermique | Heat Protectant', 'Protects up to 180°C',
        'kerastase-resistance-ciment-thermique-en', 'en', 75)
on conflict (description_id) do nothing;
/*
Generated SQL inserts similar to the provided template
where languages=['fr','en'] and store_id='65f020632bc46470c104b76f'
and manufacturer_id in (7,8,9,10,11,12) and product_type_id in (5,6,7,8)
start product id from 76
start product_description from 151
generate 15 products (76-90)
make sure to replace all ids with real number in sequence
use html format for description generator so it contain valid html tags describe the product and its features and specification and more
domain for this store is beauté
Timestamps replaced with NOW()
*/

-- Product 76: YSL Pure Shots Night Reboot Serum (Manuf: 7, Type: 5)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (76, NOW(), NOW(), true, NOW(), false, 11.0,
        false, null, true, false, 0.15, 4.5,
        'REF-YSL-SKIN-PSNIGHT76', 76, 7,
        '65f020632bc46470c104b76f', 5)
on conflict (product_id) do nothing;

-- French Description (ID: 151)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (151, NOW(), NOW(),
        '<div>
           <h2>Sérum Pure Shots Night Reboot Yves Saint Laurent</h2>
           <p>Le sérum bi-phasé exfoliant et apaisant qui régénère la peau pendant la nuit. Réduit les signes de fatigue dès le premier réveil.</p>
           <h3>Bénéfices :</h3>
           <ul>
             <li><strong>Exfoliation Douce :</strong> 3.4% d''Acide Glycolique pour éliminer les cellules mortes.</li>
             <li><strong>Apaisement & Réparation :</strong> Huile de Cactus du Maroc pour protéger et apaiser la peau.</li>
             <li><strong>Éclat Révélé :</strong> Le teint est plus lumineux, le grain de peau affiné.</li>
             <li><strong>Texture Bi-Phasée :</strong> Légère et non grasse, pénètre rapidement.</li>
           </ul>
           <h3>Conseils d''utilisation :</h3>
           <p>Agiter le flacon. Appliquer 2 pompes sur le visage nettoyé le soir, avant la crème de nuit. Éviter le contour des yeux.</p>
           <h3>Spécifications :</h3>
           <ul>
             <li><strong>Gamme :</strong> Pure Shots</li>
             <li><strong>Contenance :</strong> 30ml</li>
             <li><strong>Type de peau :</strong> Tous types, idéal peaux fatiguées, ternes.</li>
           </ul>
         </div>',
        'YSL Pure Shots Night Reboot Sérum', 'Yves Saint Laurent Pure Shots Night Reboot Sérum Régénérant Nuit',
        'Régénérez votre peau pendant la nuit avec le sérum Pure Shots Night Reboot d''YSL à l''acide glycolique et huile de cactus.',
        'YSL, Pure Shots, Night Reboot, sérum, nuit, régénérant, exfoliant, acide glycolique, huile de cactus',
        'Sérum YSL Pure Shots Night Reboot | Régénérant Nuit', 'Exfolie et apaise', 'ysl-pure-shots-night-reboot-serum',
        'fr', 76)
on conflict (description_id) do nothing;

-- English Description (ID: 152)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (152, NOW(), NOW(),
        '<div>
           <h2>Yves Saint Laurent Pure Shots Night Reboot Serum</h2>
           <p>The bi-phase exfoliating and soothing serum that regenerates skin overnight. Reduces signs of fatigue from the first morning.</p>
           <h3>Benefits:</h3>
           <ul>
             <li><strong>Gentle Exfoliation:</strong> 3.4% Glycolic Acid to remove dead cells.</li>
             <li><strong>Soothing & Repairing:</strong> Moonlight Cactus Flower Oil from Morocco to protect and soothe the skin.</li>
             <li><strong>Radiance Revealed:</strong> Complexion is brighter, skin texture refined.</li>
             <li><strong>Bi-Phase Texture:</strong> Lightweight and non-greasy, absorbs quickly.</li>
           </ul>
           <h3>How to Use:</h3>
           <p>Shake the bottle. Apply 2 pumps to cleansed face in the evening, before night cream. Avoid the eye area.</p>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Range:</strong> Pure Shots</li>
             <li><strong>Volume:</strong> 30ml</li>
             <li><strong>Skin Type:</strong> All types, ideal for tired, dull skin.</li>
           </ul>
         </div>',
        'YSL Pure Shots Night Reboot Serum', 'Yves Saint Laurent Pure Shots Night Reboot Resurfacing Serum',
        'Regenerate your skin overnight with YSL Pure Shots Night Reboot serum with glycolic acid and cactus oil.',
        'YSL, Pure Shots, Night Reboot, serum, night, regenerating, exfoliating, glycolic acid, cactus oil',
        'Serum YSL Pure Shots Night Reboot | Night Resurfacing', 'Exfoliates and soothes',
        'ysl-pure-shots-night-reboot-serum-en', 'en', 76)
on conflict (description_id) do nothing;

-- Product 77: Guerlain Aqua Allegoria Mandarine Basilic EDT (Manuf: 8, Type: 7)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (77, NOW(), NOW(), true, NOW(), false, 13.0,
        false, null, true, false, 0.4, 6.0, -- Approx for 75ml bottle
        'REF-GUER-FRAG-AAMANDBAS77', 77, 8,
        '65f020632bc46470c104b76f', 7)
on conflict (product_id) do nothing;

-- French Description (ID: 153)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (153, NOW(), NOW(),
        '<div>
           <h2>Eau de Toilette Aqua Allegoria Mandarine Basilic Guerlain</h2>
           <p>Une mandarine juteuse et acidulée transcendée par la fraîcheur aromatique du basilic. Une création pétillante et joyeuse de la collection Aqua Allegoria.</p>
           <h3>Composition Olfactive :</h3>
           <ul>
             <li><strong>Notes de Tête :</strong> Mandarine, Basilic, Lierre</li>
             <li><strong>Notes de Cœur :</strong> Thé Vert, Camomille</li>
             <li><strong>Notes de Fond :</strong> Bois de Santal, Ambre</li>
           </ul>
           <h3>La Collection Aqua Allegoria :</h3>
           <p>Une collection de Eaux Fraîches à la sophistication d''un parfum Guerlain et la fraîcheur d''une Cologne. Le fil rouge de la collection est la bergamote, agrume précieux, "l’Or vert de Calabre".</p>
           <h3>Spécifications :</h3>
           <ul>
             <li><strong>Type :</strong> Eau de Toilette</li>
             <li><strong>Famille Olfactive :</strong> Hespéridé Aromatique</li>
             <li><strong>Contenance :</strong> 75ml (Exemple)</li>
           </ul>
         </div>',
        'Guerlain Aqua Allegoria Mandarine Basilic EDT',
        'Guerlain Aqua Allegoria Mandarine Basilic Eau de Toilette Fraîche',
        'Découvrez Mandarine Basilic d''Aqua Allegoria, une eau de toilette hespéridée aromatique pétillante.',
        'Guerlain, Aqua Allegoria, Mandarine Basilic, eau de toilette, parfum mixte, hespéridé, aromatique, frais',
        'Guerlain Aqua Allegoria Mandarine Basilic | Hespéridé Aromatique', 'Mandarine juteuse & Basilic frais',
        'guerlain-aqua-allegoria-mandarine-basilic-edt', 'fr', 77)
on conflict (description_id) do nothing;

-- English Description (ID: 154)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (154, NOW(), NOW(),
        '<div>
           <h2>Guerlain Aqua Allegoria Mandarine Basilic EDT</h2>
           <p>A juicy and tangy mandarin orange transcended by the aromatic freshness of basil. A sparkling and joyful creation from the Aqua Allegoria collection.</p>
           <h3>Olfactive Composition:</h3>
           <ul>
             <li><strong>Top Notes:</strong> Mandarin Orange, Basil, Ivy</li>
             <li><strong>Heart Notes:</strong> Green Tea, Chamomile</li>
             <li><strong>Base Notes:</strong> Sandalwood, Amber</li>
           </ul>
           <h3>The Aqua Allegoria Collection:</h3>
           <p>A collection of Fresh Eaux with the sophistication of a Guerlain perfume and the freshness of a Cologne. The common thread of the collection is bergamot, a precious citrus fruit, "Calabrian green gold".</p>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Type:</strong> Eau de Toilette</li>
             <li><strong>Olfactive Family:</strong> Citrus Aromatic</li>
             <li><strong>Volume:</strong> 75ml (Example)</li>
           </ul>
         </div>',
        'Guerlain Aqua Allegoria Mandarine Basilic EDT',
        'Guerlain Aqua Allegoria Mandarine Basilic Fresh Eau de Toilette',
        'Discover Mandarine Basilic from Aqua Allegoria, a sparkling citrus aromatic eau de toilette.',
        'Guerlain, Aqua Allegoria, Mandarine Basilic, eau de toilette, unisex perfume, citrus, aromatic, fresh',
        'Guerlain Aqua Allegoria Mandarine Basilic | Citrus Aromatic', 'Juicy mandarin & Fresh basil',
        'guerlain-aqua-allegoria-mandarine-basilic-edt-en', 'en', 77)
on conflict (description_id) do nothing;

-- Product 78: Shiseido Tsubaki Premium Repair Mask (Manuf: 9, Type: 8)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (78, NOW(), NOW(), true, NOW(), false, 8.0,
        false, null, true, false, 0.22, 8.5, -- Jar dimensions
        'REF-SHIS-HAIR-TSUBAKIMASK78', 78, 9,
        '65f020632bc46470c104b76f', 8)
on conflict (product_id) do nothing;

-- French Description (ID: 155)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (155, NOW(), NOW(),
        '<div>
           <h2>Masque Capillaire Tsubaki Premium Repair Shiseido</h2>
           <p>Masque capillaire japonais à rincer immédiatement (0 seconde d''attente !) pour une réparation intense et une douceur digne d''un salon.</p>
           <h3>Bénéfices :</h3>
           <ul>
             <li><strong>Réparation Instantanée :</strong> Technologie innovante qui pénètre au cœur du cheveu pour réparer les dommages.</li>
             <li><strong>0 Seconde d''Attente :</strong> Rincer immédiatement après application pour un résultat optimal.</li>
             <li><strong>Nutrition & Brillance :</strong> Huile de Tsubaki (Camélia) et Gelée Royale pour nourrir et faire briller.</li>
             <li><strong>Toucher Soyeux :</strong> Laisse les cheveux incroyablement doux et lisses.</li>
           </ul>
           <h3>Conseils d''utilisation :</h3>
           <p>Après le shampooing et l''après-shampooing, essorer les cheveux. Appliquer une quantité appropriée sur l''ensemble de la chevelure, puis rincer immédiatement et abondamment. Utiliser 1 à 2 fois par semaine.</p>
           <h3>Spécifications :</h3>
           <ul>
             <li><strong>Gamme :</strong> Tsubaki</li>
             <li><strong>Contenance :</strong> 180g</li>
             <li><strong>Type de cheveux :</strong> Abîmés, Secs</li>
           </ul>
         </div>',
        'Shiseido Tsubaki Premium Repair Masque Capillaire',
        'Shiseido Tsubaki Premium Repair Masque Réparation Intense 0 Seconde',
        'Réparez vos cheveux en 0 seconde avec le masque Tsubaki Premium Repair de Shiseido.',
        'Shiseido, Tsubaki, masque cheveux, réparation intense, huile de camélia, cheveux abîmés',
        'Masque Shiseido Tsubaki Premium Repair | Réparation 0 Seconde', 'Réparation intense en 0 seconde',
        'shiseido-tsubaki-premium-repair-mask', 'fr', 78)
on conflict (description_id) do nothing;

-- English Description (ID: 156)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (156, NOW(), NOW(),
        '<div>
           <h2>Shiseido Tsubaki Premium Repair Hair Mask</h2>
           <p>Japanese hair mask to rinse off immediately (0 second wait!) for intense repair and salon-like smoothness.</p>
           <h3>Benefits:</h3>
           <ul>
             <li><strong>Instant Repair:</strong> Innovative technology penetrates the hair core to repair damage.</li>
             <li><strong>0 Second Wait Time:</strong> Rinse immediately after application for optimal results.</li>
             <li><strong>Nutrition & Shine:</strong> Tsubaki (Camellia) Oil and Royal Jelly to nourish and add shine.</li>
             <li><strong>Silky Touch:</strong> Leaves hair incredibly soft and smooth.</li>
           </ul>
           <h3>How to Use:</h3>
           <p>After shampoo and conditioner, squeeze out excess water. Apply an appropriate amount to the entire hair, then rinse immediately and thoroughly. Use 1-2 times a week.</p>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Range:</strong> Tsubaki</li>
             <li><strong>Content:</strong> 180g</li>
             <li><strong>Hair Type:</strong> Damaged, Dry</li>
           </ul>
         </div>',
        'Shiseido Tsubaki Premium Repair Hair Mask',
        'Shiseido Tsubaki Premium Repair 0 Second Wait Intense Repair Mask',
        'Repair your hair in 0 seconds with Shiseido Tsubaki Premium Repair mask.',
        'Shiseido, Tsubaki, hair mask, intense repair, camellia oil, damaged hair',
        'Mask Shiseido Tsubaki Premium Repair | 0 Second Repair', 'Intense repair in 0 seconds',
        'shiseido-tsubaki-premium-repair-mask-en', 'en', 78)
on conflict (description_id) do nothing;

-- Product 79: NARS Afterglow Lip Balm (Manuf: 10, Type: 6)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (79, NOW(), NOW(), true, NOW(), false, 7.0,
        false, null, true, false, 0.03, 2.0,
        'REF-NARS-MAKE-AFTERGLOWLB79', 79, 10,
        '65f020632bc46470c104b76f', 6)
on conflict (product_id) do nothing;

-- French Description (ID: 157)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (157, NOW(), NOW(),
        '<div>
           <h2>Baume à Lèvres Afterglow NARS</h2>
           <p>Un baume à lèvres hydratant teinté qui offre une touche de couleur subtile et un confort longue durée.</p>
           <h3>Bénéfices :</h3>
           <ul>
             <li><strong>Hydratation Confortable :</strong> Complexe Monoï Hydratant pour des lèvres douces et souples.</li>
             <li><strong>Touche de Couleur :</strong> Voile de couleur transparent et lumineux.</li>
             <li><strong>Protection Antioxydante :</strong> Aide à protéger les lèvres.</li>
             <li><strong>Fini Brillant :</strong> Apporte une brillance subtile.</li>
           </ul>
           <h3>Conseils d''application :</h3>
           <p>Appliquer directement sur les lèvres pour une touche de couleur hydratante. Peut être porté seul ou sous un autre produit lèvres.</p>
           <h3>Spécifications :</h3>
           <ul>
             <li><strong>Gamme :</strong> Afterglow Lip Balm</li>
             <li><strong>Teinte :</strong> Plusieurs teintes disponibles (Exemple: Orgasm - Rose pêche doré)</li>
             <li><strong>Contenance :</strong> 3g</li>
           </ul>
         </div>',
        'NARS Afterglow Baume à Lèvres', 'NARS Afterglow Baume à Lèvres Hydratant Teinté',
        'Hydratez vos lèvres avec une touche de couleur grâce au baume Afterglow de NARS.',
        'NARS, Afterglow, baume à lèvres, lip balm, hydratant, teinté, maquillage lèvres',
        'Baume Lèvres NARS Afterglow | Hydratant Teinté', 'Hydratation & Couleur subtile', 'nars-afterglow-lip-balm',
        'fr', 79)
on conflict (description_id) do nothing;

-- English Description (ID: 158)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (158, NOW(), NOW(),
        '<div>
           <h2>NARS Afterglow Lip Balm</h2>
           <p>A hydrating tinted lip balm that delivers a sheer touch of color and long-lasting comfort.</p>
           <h3>Benefits:</h3>
           <ul>
             <li><strong>Comfortable Hydration:</strong> Monoï Hydrating Complex for soft, supple lips.</li>
             <li><strong>Hint of Color:</strong> Sheer, luminous veil of color.</li>
             <li><strong>Antioxidant Protection:</strong> Helps protect lips.</li>
             <li><strong>Glossy Finish:</strong> Provides a subtle shine.</li>
           </ul>
           <h3>Application Tips:</h3>
           <p>Apply directly to lips for a hydrating hint of color. Can be worn alone or under other lip products.</p>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Range:</strong> Afterglow Lip Balm</li>
             <li><strong>Shade:</strong> Several shades available (Example: Orgasm - Peachy pink with golden shimmer)</li>
             <li><strong>Content:</strong> 3g</li>
           </ul>
         </div>',
        'NARS Afterglow Lip Balm', 'NARS Afterglow Hydrating Tinted Lip Balm',
        'Hydrate your lips with a hint of color using NARS Afterglow balm.',
        'NARS, Afterglow, lip balm, hydrating, tinted, lip makeup', 'Lip Balm NARS Afterglow | Hydrating Tinted',
        'Hydration & Subtle color', 'nars-afterglow-lip-balm-en', 'en', 79)
on conflict (description_id) do nothing;

-- Product 80: La Roche-Posay Lipikar Baume AP+M (Manuf: 11, Type: 5)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (80, NOW(), NOW(), true, NOW(), false, 20.0,
        false, null, true, false, 0.45, 9.0, -- Approx for 400ml pump bottle
        'REF-LRP-SKIN-LIPIAPM80', 80, 11,
        '65f020632bc46470c104b76f', 5)
on conflict (product_id) do nothing;

-- French Description (ID: 159)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (159, NOW(), NOW(),
        '<div>
           <h2>Baume Lipikar AP+M La Roche-Posay</h2>
           <p>Baume triple-réparation. Apaise immédiatement. Anti-grattage, anti-rechute. Pour peaux très sèches, à tendance à l''eczéma atopique. Convient aux nourrissons, enfants, adultes.</p>
           <h3>Bénéfices :</h3>
           <ul>
             <li><strong>Apaise Immédiatement :</strong> Niacinamide pour calmer les sensations de démangeaisons.</li>
             <li><strong>Action Anti-Grattage & Anti-Rechute :</strong> Technologie AP+M [Aqua Posae + Microresyl] pour rééquilibrer le microbiome cutané.</li>
             <li><strong>Répare la Barrière Cutanée :</strong> Beurre de Karité (20%) et Huile de Canola riches en Oméga 3 et 6.</li>
             <li><strong>Absorption Rapide :</strong> Texture non grasse, non collante.</li>
           </ul>
           <h3>Conseils d''utilisation :</h3>
           <p>Appliquer 1 fois par jour sur le visage et/ou le corps préalablement nettoyé avec un produit doux relipidant comme Lipikar Syndet AP+ ou Huile Lavante AP+.</p>
           <h3>Spécifications :</h3>
           <ul>
             <li><strong>Gamme :</strong> Lipikar</li>
             <li><strong>Type de peau :</strong> Très sèche, à tendance atopique, sujette aux démangeaisons.</li>
             <li><strong>Contenance :</strong> 400ml (Exemple)</li>
             <li><strong>Formule :</strong> Sans parfum.</li>
           </ul>
         </div>',
        'La Roche-Posay Lipikar Baume AP+M', 'La Roche-Posay Lipikar Baume AP+M Triple-Réparation Anti-Grattage',
        'Apaisez et réparez les peaux très sèches à tendance atopique avec le baume Lipikar AP+M de La Roche-Posay.',
        'La Roche-Posay, Lipikar, Baume AP+M, peau atopique, eczéma, anti-grattage, beurre de karité, niacinamide',
        'Baume La Roche-Posay Lipikar AP+M | Peau Atopique', 'Anti-grattage, Anti-rechute',
        'la-roche-posay-lipikar-baume-ap-m', 'fr', 80)
on conflict (description_id) do nothing;

-- English Description (ID: 160)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (160, NOW(), NOW(),
        '<div>
           <h2>La Roche-Posay Lipikar Balm AP+M</h2>
           <p>Triple-action balm. Immediately soothes. Anti-scratching, anti-relapse. For very dry, eczema-prone skin. Suitable for babies, children, adults.</p>
           <h3>Benefits:</h3>
           <ul>
             <li><strong>Immediately Soothes:</strong> Niacinamide to calm itching sensations.</li>
             <li><strong>Anti-Scratching & Anti-Relapse Action:</strong> AP+M Technology [Aqua Posae + Microresyl] to rebalance the skin microbiome.</li>
             <li><strong>Repairs Skin Barrier:</strong> Shea Butter (20%) and Canola Oil rich in Omega 3 and 6.</li>
             <li><strong>Fast Absorption:</strong> Non-greasy, non-sticky texture.</li>
           </ul>
           <h3>How to Use:</h3>
           <p>Apply once daily to face and/or body after cleansing with a gentle lipid-replenishing product like Lipikar Syndet AP+ or Cleansing Oil AP+.</p>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Range:</strong> Lipikar</li>
             <li><strong>Skin Type:</strong> Very dry, atopy-prone, itchy skin.</li>
             <li><strong>Volume:</strong> 400ml (Example)</li>
             <li><strong>Formula:</strong> Fragrance-free.</li>
           </ul>
         </div>',
        'La Roche-Posay Lipikar Balm AP+M', 'La Roche-Posay Lipikar Balm AP+M Triple-Action Anti-Scratching Balm',
        'Soothe and repair very dry, atopy-prone skin with La Roche-Posay Lipikar Balm AP+M.',
        'La Roche-Posay, Lipikar, Balm AP+M, atopic skin, eczema, anti-scratching, shea butter, niacinamide',
        'Balm La Roche-Posay Lipikar AP+M | Atopic Skin', 'Anti-scratching, Anti-relapse',
        'la-roche-posay-lipikar-balm-ap-m-en', 'en', 80)
on conflict (description_id) do nothing;

-- Product 81: Kérastase Nutritive 8H Magic Night Serum (Manuf: 12, Type: 8)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (81, NOW(), NOW(), true, NOW(), false, 15.0,
        false, null, true, false, 0.15, 4.5, -- Approx for 90ml bottle
        'REF-KERA-HAIR-NUTRI8HSERUM81', 81, 12,
        '65f020632bc46470c104b76f', 8)
on conflict (product_id) do nothing;

-- French Description (ID: 161)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (161, NOW(), NOW(),
        '<div>
           <h2>Sérum de Nuit 8H Magic Night Serum Nutritive Kérastase</h2>
           <p>Sérum capillaire de nuit sans rinçage pour cheveux secs. Nourrit intensément pendant 8h pour des cheveux doux, souples et faciles à coiffer au réveil.</p>
           <h3>Bénéfices :</h3>
           <ul>
             <li><strong>Nutrition Nocturne 8h :</strong> Infuse les nutriments essentiels pendant le sommeil.</li>
             <li><strong>Réparation & Douceur :</strong> Extrait de Racine d''Iris et Mélange de 5 Vitamines pour revitaliser la fibre.</li>
             <li><strong>Absorption Progressive :</strong> Texture crème légère qui ne laisse aucun résidu sur l''oreiller.</li>
             <li><strong>Parfum Évolutif :</strong> Favorise la relaxation au coucher et l''énergie au réveil.</li>
           </ul>
           <h3>Conseils d''utilisation :</h3>
           <p>Appliquer 2 à 4 pressions sur les longueurs et pointes des cheveux secs ou essorés avant le coucher. Ne pas rincer.</p>
           <h3>Spécifications :</h3>
           <ul>
             <li><strong>Gamme :</strong> Nutritive</li>
             <li><strong>Type de cheveux :</strong> Secs</li>
             <li><strong>Contenance :</strong> 90ml</li>
           </ul>
         </div>',
        'Kérastase Nutritive 8H Magic Night Serum', 'Kérastase Nutritive Sérum de Nuit Magique 8H Cheveux Secs',
        'Nourrissez vos cheveux secs pendant la nuit avec le 8H Magic Night Serum Nutritive de Kérastase.',
        'Kérastase, Nutritive, 8H Magic Night Serum, sérum nuit, cheveux secs, nutrition',
        'Sérum Kérastase Nutritive 8H Magic Night | Cheveux Secs', 'Nutrition nocturne 8h',
        'kerastase-nutritive-8h-magic-night-serum', 'fr', 81)
on conflict (description_id) do nothing;

-- English Description (ID: 162)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (162, NOW(), NOW(),
        '<div>
           <h2>Kérastase Nutritive 8H Magic Night Serum</h2>
           <p>Leave-in overnight hair serum for dry hair. Intensely nourishes for 8 hours for soft, supple, and easy-to-style hair upon waking.</p>
           <h3>Benefits:</h3>
           <ul>
             <li><strong>8h Overnight Nutrition:</strong> Infuses essential nutrients during sleep.</li>
             <li><strong>Repair & Softness:</strong> Iris Root extract and a Blend of 5 Vitamins to revitalize the fiber.</li>
             <li><strong>Progressive Absorption:</strong> Lightweight cream texture leaves no residue on the pillow.</li>
             <li><strong>Evolving Fragrance:</strong> Promotes relaxation at bedtime and energy upon waking.</li>
           </ul>
           <h3>How to Use:</h3>
           <p>Apply 2 to 4 pumps to lengths and ends of dry or towel-dried hair before bed. Leave-in.</p>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Range:</strong> Nutritive</li>
             <li><strong>Hair Type:</strong> Dry</li>
             <li><strong>Volume:</strong> 90ml</li>
           </ul>
         </div>',
        'Kérastase Nutritive 8H Magic Night Serum', 'Kérastase Nutritive 8H Magic Night Serum for Dry Hair',
        'Nourish your dry hair overnight with Kérastase Nutritive 8H Magic Night Serum.',
        'Kérastase, Nutritive, 8H Magic Night Serum, night serum, dry hair, nutrition',
        'Serum Kérastase Nutritive 8H Magic Night | Dry Hair', '8h overnight nutrition',
        'kerastase-nutritive-8h-magic-night-serum-en', 'en', 81)
on conflict (description_id) do nothing;

-- Product 82: YSL All Hours Foundation (Manuf: 7, Type: 6)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (82, NOW(), NOW(), true, NOW(), false, 11.5,
        false, null, true, false, 0.11, 3.5,
        'REF-YSL-MAKE-ALLHOURSFND82', 82, 7,
        '65f020632bc46470c104b76f', 6)
on conflict (product_id) do nothing;

-- French Description (ID: 163)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (163, NOW(), NOW(),
        '<div>
           <h2>Fond de Teint All Hours Yves Saint Laurent</h2>
           <p>Le fond de teint mat lumineux tenue 24h*. Couvrance totale, sensation peau nue et infusion de soin à l''acide hyaluronique.</p>
           <h3>Bénéfices :</h3>
           <ul>
             <li><strong>Tenue 24h* & Fini Mat Lumineux :</strong> Teint impeccable et frais toute la journée.</li>
             <li><strong>Couvrance Totale & Légèreté :</strong> Camoufle les imperfections sans effet masque.</li>
             <li><strong>Infusion de Soin :</strong> Acide Hyaluronique pour l''hydratation et extraits de Jasmin pour l''action anti-teint terne.</li>
             <li><strong>Transfert-Proof & Waterproof :</strong> Résiste à l''épreuve du quotidien. Protection SPF 39 PA+++.</li>
           </ul>
           <h3>Technologie Skin-Fusing™ :</h3>
           <p>Pigments fins qui fusionnent avec la peau pour un résultat naturel.</p>
           <h3>Spécifications :</h3>
           <ul>
             <li><strong>Gamme :</strong> All Hours</li>
             <li><strong>Fini :</strong> Mat Lumineux</li>
             <li><strong>Contenance :</strong> 25ml</li>
             <li><strong>Teinte :</strong> Large gamme (Exemple: LW4 - Light Warm)</li>
           </ul>
           <p><small>*Test consommateur sur 111 femmes.</small></p>
         </div>',
        'YSL All Hours Fond de Teint', 'Yves Saint Laurent All Hours Fond de Teint Mat Lumineux Tenue 24h SPF 39',
        'Obtenez un teint mat lumineux et une tenue 24h avec le fond de teint All Hours d''YSL.',
        'YSL, All Hours, fond de teint, maquillage teint, mat lumineux, tenue 24h, couvrance totale, SPF 39',
        'Fond de Teint YSL All Hours | Mat Lumineux 24h', 'Mat lumineux, Tenue 24h', 'ysl-all-hours-foundation', 'fr',
        82)
on conflict (description_id) do nothing;

-- English Description (ID: 164)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (164, NOW(), NOW(),
        '<div>
           <h2>Yves Saint Laurent All Hours Foundation</h2>
           <p>The 24h* luminous matte foundation. Full coverage, weightless feel, and skincare infusion with hyaluronic acid.</p>
           <h3>Benefits:</h3>
           <ul>
             <li><strong>24h* Wear & Luminous Matte Finish:</strong> Flawless, fresh complexion all day long.</li>
             <li><strong>Full Coverage & Weightless:</strong> Conceals imperfections without a mask effect.</li>
             <li><strong>Skincare Infusion:</strong> Hyaluronic Acid for hydration and Jasmine Petals extracts for anti-dullness action.</li>
             <li><strong>Transfer-Proof & Waterproof:</strong> Withstands daily life. SPF 39 PA+++ protection.</li>
           </ul>
           <h3>Skin-Fusing™ Technology:</h3>
           <p>Fine pigments that fuse with the skin for a natural result.</p>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Range:</strong> All Hours</li>
             <li><strong>Finish:</strong> Luminous Matte</li>
             <li><strong>Volume:</strong> 25ml</li>
             <li><strong>Shade:</strong> Wide range (Example: LW4 - Light Warm)</li>
           </ul>
           <p><small>*Consumer test on 111 women.</small></p>
         </div>',
        'YSL All Hours Foundation', 'Yves Saint Laurent All Hours Luminous Matte 24h Wear Foundation SPF 39',
        'Achieve a luminous matte complexion and 24h wear with YSL All Hours foundation.',
        'YSL, All Hours, foundation, face makeup, luminous matte, 24h wear, full coverage, SPF 39',
        'Foundation YSL All Hours | Luminous Matte 24h', 'Luminous matte, 24h Wear', 'ysl-all-hours-foundation-en',
        'en', 82)
on conflict (description_id) do nothing;

-- Product 83: Guerlain Super Aqua-Serum (Manuf: 8, Type: 5)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (83, NOW(), NOW(), true, NOW(), false, 15.0,
        false, null, true, false, 0.25, 5.0,
        'REF-GUER-SKIN-SUPERAQUA83', 83, 8,
        '65f020632bc46470c104b76f', 5)
on conflict (product_id) do nothing;

-- French Description (ID: 165)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (165, NOW(), NOW(),
        '<div>
           <h2>Sérum Hydratant Intense Super Aqua-Serum Guerlain</h2>
           <p>L''hydratant intense anti-rides iconique de Guerlain. Repulpe la peau et lisse les rides pour une peau visiblement plus jeune et éclatante.</p>
           <h3>Technologie Aquacomplex Advanced :</h3>
           <ul>
             <li><strong>Hydratation Intense :</strong> Favorise les flux d’eau au cœur de la peau pour une réhydratation immédiate et autonome*.</li>
             <li><strong>Action Anti-Rides :</strong> Repulpe la peau de l''intérieur pour lisser rides et ridules.</li>
             <li><strong>Protection Anti-Âge :</strong> Protège la peau du risque de déshydratation future, cause majeure du vieillissement cutané.</li>
             <li><strong>Texture Fraîche et Veloutée :</strong> Pénètre instantanément.</li>
           </ul>
           <h3>Résultats :</h3>
           <p>La peau est intensément hydratée, repulpée, lissée et éclatante.</p>
           <h3>Spécifications :</h3>
           <ul>
             <li><strong>Gamme :</strong> Super Aqua-Serum</li>
             <li><strong>Contenance :</strong> 50ml (Exemple)</li>
             <li><strong>Type de peau :</strong> Tous types, idéal peaux déshydratées.</li>
           </ul>
           <p><small>*Tests in vitro sur ingrédients.</small></p>
         </div>',
        'Guerlain Super Aqua-Serum Hydratant Intense', 'Guerlain Super Aqua-Serum Sérum Hydratant Intense Défense Âge',
        'Hydratez intensément et lissez les rides avec le Super Aqua-Serum de Guerlain.',
        'Guerlain, Super Aqua-Serum, sérum, hydratant, anti-rides, peau déshydratée, Aquacomplex',
        'Sérum Guerlain Super Aqua | Hydratant Intense', 'Hydratation intense anti-rides', 'guerlain-super-aqua-serum',
        'fr', 83)
on conflict (description_id) do nothing;

-- English Description (ID: 166)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (166, NOW(), NOW(),
        '<div>
           <h2>Guerlain Super Aqua-Serum Intense Hydration Wrinkle Plumper</h2>
           <p>Guerlain''s iconic intense anti-wrinkle hydrator. Plumps the skin and smooths wrinkles for visibly younger, radiant skin.</p>
           <h3>Aquacomplex Advanced Technology:</h3>
           <ul>
             <li><strong>Intense Hydration:</strong> Promotes water flow within the skin for immediate and self-sufficient rehydration*.</li>
             <li><strong>Anti-Wrinkle Action:</strong> Plumps the skin from within to smooth wrinkles and fine lines.</li>
             <li><strong>Age-Defying Protection:</strong> Protects the skin from the risk of future dehydration, a major cause of skin aging.</li>
             <li><strong>Fresh, Velvety Texture:</strong> Penetrates instantly.</li>
           </ul>
           <h3>Results:</h3>
           <p>Skin is intensely hydrated, plumped, smoothed, and radiant.</p>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Range:</strong> Super Aqua-Serum</li>
             <li><strong>Volume:</strong> 50ml (Example)</li>
             <li><strong>Skin Type:</strong> All types, ideal for dehydrated skin.</li>
           </ul>
           <p><small>*In vitro tests on ingredients.</small></p>
         </div>',
        'Guerlain Super Aqua-Serum Intense Hydrator', 'Guerlain Super Aqua-Serum Intense Hydration Age Defense Serum',
        'Intensely hydrate and smooth wrinkles with Guerlain Super Aqua-Serum.',
        'Guerlain, Super Aqua-Serum, serum, hydrating, anti-wrinkle, dehydrated skin, Aquacomplex',
        'Serum Guerlain Super Aqua | Intense Hydrator', 'Intense anti-wrinkle hydration',
        'guerlain-super-aqua-serum-en', 'en', 83)
on conflict (description_id) do nothing;

-- Product 84: Shiseido Ginza Eau de Parfum (Manuf: 9, Type: 7)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (84, NOW(), NOW(), true, NOW(), false, 12.0,
        false, null, true, false, 0.3, 5.0, -- Approx for 50ml
        'REF-SHIS-FRAG-GINZAEDP84', 84, 9,
        '65f020632bc46470c104b76f', 7)
on conflict (product_id) do nothing;

-- French Description (ID: 167)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (167, NOW(), NOW(),
        '<div>
           <h2>Eau de Parfum Ginza Shiseido</h2>
           <p>Ginza, le nouveau parfum pour femme de Shiseido. L’équilibre parfait entre force et féminité, subtilité et passion.</p>
           <h3>Composition Olfactive :</h3>
           <p>Un bouquet floral aiguisé par des notes boisées. L''élégance naturelle des fleurs rencontre la puissance des bois sacrés.</p>
           <ul>
             <li><strong>Notes de Tête :</strong> Grenade, Poivre Rose</li>
             <li><strong>Notes de Cœur :</strong> Jasmin, Magnolia, Freesia, Orchidée Japonaise</li>
             <li><strong>Notes de Fond :</strong> Bois d''Hinoki, Patchouli, Santal</li>
           </ul>
           <h3>Le Flacon :</h3>
           <p>Une dague noire traverse le flacon, symbole de la force intérieure de chaque femme. Design unique par Constance Guisset.</p>
           <h3>Spécifications :</h3>
           <ul>
             <li><strong>Type :</strong> Eau de Parfum</li>
             <li><strong>Famille Olfactive :</strong> Floral Boisé Musqué</li>
             <li><strong>Contenance :</strong> 50ml (Exemple)</li>
           </ul>
         </div>',
        'Shiseido Ginza Eau de Parfum', 'Shiseido Ginza Eau de Parfum - Parfum Floral Boisé Femme',
        'Découvrez Ginza de Shiseido, un parfum floral boisé célébrant la dualité féminine.',
        'Shiseido, Ginza, eau de parfum, parfum femme, floral, boisé, jasmin, hinoki',
        'Shiseido Ginza Eau de Parfum | Floral Boisé', 'Bouquet floral & Bois sacrés', 'shiseido-ginza-eau-de-parfum',
        'fr', 84)
on conflict (description_id) do nothing;

-- English Description (ID: 168)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (168, NOW(), NOW(),
        '<div>
           <h2>Shiseido Ginza Eau de Parfum</h2>
           <p>Ginza, the new fragrance for women by Shiseido. The perfect balance between strength and femininity, subtlety and passion.</p>
           <h3>Olfactive Composition:</h3>
           <p>A floral bouquet sharpened by woody notes. The natural elegance of flowers meets the power of sacred woods.</p>
           <ul>
             <li><strong>Top Notes:</strong> Pomegranate, Pink Pepper</li>
             <li><strong>Heart Notes:</strong> Jasmine, Magnolia, Freesia, Japanese Orchid</li>
             <li><strong>Base Notes:</strong> Hinoki Wood, Patchouli, Sandalwood</li>
           </ul>
           <h3>The Bottle:</h3>
           <p>A black dagger pierces the bottle, symbolizing the inner strength of every woman. Unique design by Constance Guisset.</p>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Type:</strong> Eau de Parfum</li>
             <li><strong>Olfactive Family:</strong> Floral Woody Musk</li>
             <li><strong>Volume:</strong> 50ml (Example)</li>
           </ul>
         </div>',
        'Shiseido Ginza Eau de Parfum', 'Shiseido Ginza Eau de Parfum - Women''s Floral Woody Perfume',
        'Discover Shiseido Ginza, a floral woody fragrance celebrating feminine duality.',
        'Shiseido, Ginza, eau de parfum, women perfume, floral, woody, jasmine, hinoki',
        'Shiseido Ginza Eau de Parfum | Floral Woody', 'Floral bouquet & Sacred woods',
        'shiseido-ginza-eau-de-parfum-en', 'en', 84)
on conflict (description_id) do nothing;

-- Product 85: NARS Light Reflecting Setting Powder (Manuf: 10, Type: 6)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (85, NOW(), NOW(), true, NOW(), false, 2.0,
        false, null, true, false, 0.08, 7.0, -- Compact dimensions
        'REF-NARS-MAKE-LRSPWD85', 85, 10,
        '65f020632bc46470c104b76f', 6)
on conflict (product_id) do nothing;

-- French Description (ID: 169)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (169, NOW(), NOW(),
        '<div>
           <h2>Poudre Fixante Light Reflecting Setting Powder NARS</h2>
           <p>Une poudre fixante culte, invisible et légère, qui sublime le maquillage et floute l''apparence des ridules et des pores.</p>
           <h3>Bénéfices :</h3>
           <ul>
             <li><strong>Fini Invisible :</strong> Formule légère qui s''adapte à toutes les carnations.</li>
             <li><strong>Effet Floutant :</strong> Technologie photochromique qui diffuse la lumière et s''ajuste aux nouvelles sources de lumière.</li>
             <li><strong>Fixe le Maquillage :</strong> Prolonge la tenue du fond de teint et contrôle la brillance.</li>
             <li><strong>Texture Soyeuse :</strong> Poudre minérale ultra-fine enrichie en glycérine et vitamine E.</li>
           </ul>
           <h3>Conseils d''application :</h3>
           <p>Prélever la poudre avec un pinceau adapté. Tapoter l''excédent et appliquer sur l''ensemble du visage en insistant sur la zone T.</p>
           <h3>Spécifications :</h3>
           <ul>
             <li><strong>Gamme :</strong> Light Reflecting Setting Powder</li>
             <li><strong>Type :</strong> Poudre Libre ou Pressée</li>
             <li><strong>Teinte :</strong> Crystal (Translucide)</li>
             <li><strong>Contenance :</strong> 10g (Libre) / 7g (Pressée)</li>
           </ul>
         </div>',
        'NARS Light Reflecting Setting Powder', 'NARS Poudre Fixante Libre ou Pressée Light Reflecting',
        'Fixez votre maquillage et obtenez un fini lumineux et flouté avec la poudre Light Reflecting de NARS.',
        'NARS, Light Reflecting Setting Powder, poudre fixante, poudre libre, poudre pressée, finition, maquillage teint',
        'Poudre Fixante NARS Light Reflecting | Fini Lumineux', 'Invisible, fixe et floute',
        'nars-light-reflecting-setting-powder', 'fr', 85)
on conflict (description_id) do nothing;

-- English Description (ID: 170)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (170, NOW(), NOW(),
        '<div>
           <h2>NARS Light Reflecting Setting Powder</h2>
           <p>A cult-favorite, invisible, lightweight setting powder that enhances makeup and blurs the look of fine lines and pores.</p>
           <h3>Benefits:</h3>
           <ul>
             <li><strong>Invisible Finish:</strong> Lightweight formula adapts to all skin tones.</li>
             <li><strong>Blurring Effect:</strong> Photochromic technology diffuses light and adjusts to new sources of light.</li>
             <li><strong>Sets Makeup:</strong> Extends foundation wear and controls shine.</li>
             <li><strong>Silky Texture:</strong> Ultra-fine mineral powder enriched with glycerin and vitamin E.</li>
           </ul>
           <h3>Application Tips:</h3>
           <p>Pick up powder with a suitable brush. Tap off excess and apply all over the face, focusing on the T-zone.</p>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Range:</strong> Light Reflecting Setting Powder</li>
             <li><strong>Type:</strong> Loose or Pressed Powder</li>
             <li><strong>Shade:</strong> Crystal (Translucent)</li>
             <li><strong>Content:</strong> 10g (Loose) / 7g (Pressed)</li>
           </ul>
         </div>',
        'NARS Light Reflecting Setting Powder', 'NARS Light Reflecting Loose or Pressed Setting Powder',
        'Set your makeup and achieve a luminous, blurred finish with NARS Light Reflecting powder.',
        'NARS, Light Reflecting Setting Powder, setting powder, loose powder, pressed powder, finishing, face makeup',
        'Setting Powder NARS Light Reflecting | Luminous Finish', 'Invisible, sets & blurs',
        'nars-light-reflecting-setting-powder-en', 'en', 85)
on conflict (description_id) do nothing;

-- Product 86: La Roche-Posay Pure Vitamin C10 Serum (Manuf: 11, Type: 5)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (86, NOW(), NOW(), true, NOW(), false, 10.0,
        false, null, true, false, 0.1, 3.5,
        'REF-LRP-SKIN-VITC10-86', 86, 11,
        '65f020632bc46470c104b76f', 5)
on conflict (product_id) do nothing;

-- French Description (ID: 171)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (171, NOW(), NOW(),
        '<div>
           <h2>Sérum Pure Vitamin C10 La Roche-Posay</h2>
           <p>Le sérum rénovateur éclat anti-rides à la vitamine C pure. Révèle l''éclat des peaux sensibles, corrige rides et manque de fermeté.</p>
           <h3>Bénéfices :</h3>
           <ul>
             <li><strong>Anti-Oxydant & Éclat :</strong> 10% de Vitamine C Pure pour illuminer et unifier le teint.</li>
             <li><strong>Action Anti-Rides :</strong> Stimule la production de collagène.</li>
             <li><strong>Lissant & Apaisant :</strong> Acide Salicylique pour affiner le grain de peau et Neurosensine pour apaiser.</li>
             <li><strong>Texture Légère :</strong> Absorption rapide, fini velouté non gras.</li>
           </ul>
           <h3>Conseils d''utilisation :</h3>
           <p>Appliquer le matin sur le visage et le cou. Éviter le contour des yeux. Utiliser en association avec une protection SPF.</p>
           <h3>Spécifications :</h3>
           <ul>
             <li><strong>Gamme :</strong> Pure Vitamin C10</li>
             <li><strong>Contenance :</strong> 30ml</li>
             <li><strong>Type de peau :</strong> Tous types, y compris sensibles.</li>
           </ul>
         </div>',
        'La Roche-Posay Pure Vitamin C10 Sérum', 'La Roche-Posay Pure Vitamin C10 Sérum Rénovateur Éclat Anti-Rides',
        'Révélez l''éclat de votre peau avec le sérum Pure Vitamin C10 de La Roche-Posay.',
        'La Roche-Posay, Pure Vitamin C10, sérum, vitamine C, anti-rides, éclat, peau sensible',
        'Sérum La Roche-Posay Pure Vitamin C10 | Éclat & Anti-Rides', '10% Vitamine C Pure',
        'la-roche-posay-pure-vitamin-c10-serum', 'fr', 86)
on conflict (description_id) do nothing;

-- English Description (ID: 172)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (172, NOW(), NOW(),
        '<div>
           <h2>La Roche-Posay Pure Vitamin C10 Serum</h2>
           <p>The anti-wrinkle renovating radiance serum with pure vitamin C. Reveals sensitive skin''s radiance, corrects wrinkles and lack of firmness.</p>
           <h3>Benefits:</h3>
           <ul>
             <li><strong>Anti-Oxidant & Radiance:</strong> 10% Pure Vitamin C to brighten and even out the complexion.</li>
             <li><strong>Anti-Wrinkle Action:</strong> Stimulates collagen production.</li>
             <li><strong>Smoothing & Soothing:</strong> Salicylic Acid to refine skin texture and Neurosensine to soothe.</li>
             <li><strong>Lightweight Texture:</strong> Fast absorption, non-greasy velvety finish.</li>
           </ul>
           <h3>How to Use:</h3>
           <p>Apply morning on face and neck. Avoid eye area. Use in association with SPF protection.</p>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Range:</strong> Pure Vitamin C10</li>
             <li><strong>Volume:</strong> 30ml</li>
             <li><strong>Skin Type:</strong> All types, including sensitive.</li>
           </ul>
         </div>',
        'La Roche-Posay Pure Vitamin C10 Serum', 'La Roche-Posay Pure Vitamin C10 Anti-Wrinkle Radiance Renewing Serum',
        'Reveal your skin''s radiance with La Roche-Posay Pure Vitamin C10 serum.',
        'La Roche-Posay, Pure Vitamin C10, serum, vitamin C, anti-wrinkle, radiance, sensitive skin',
        'Serum La Roche-Posay Pure Vitamin C10 | Radiance & Anti-Wrinkle', '10% Pure Vitamin C',
        'la-roche-posay-pure-vitamin-c10-serum-en', 'en', 86)
on conflict (description_id) do nothing;

-- Product 87: Kérastase Discipline Fluidissime Spray (Manuf: 12, Type: 8)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (87, NOW(), NOW(), true, NOW(), false, 19.0,
        false, null, true, false, 0.18, 4.0, -- Approx for 150ml spray
        'REF-KERA-HAIR-DISCFLUID87', 87, 12,
        '65f020632bc46470c104b76f', 8)
on conflict (product_id) do nothing;

-- French Description (ID: 173)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (173, NOW(), NOW(),
        '<div>
           <h2>Spray Fluidissime Discipline Kérastase</h2>
           <p>Spray anti-frizz thermo-protecteur pour cheveux indisciplinés. Contrôle les frisottis, protège de la chaleur et apporte fluidité et brillance.</p>
           <h3>Bénéfices :</h3>
           <ul>
             <li><strong>Anti-Frizz 72h* :</strong> Contrôle durable des frisottis et de l''humidité.</li>
             <li><strong>Thermo-Protection :</strong> Protège de la chaleur jusqu''à 230°C.</li>
             <li><strong>Fluidité & Mouvement :</strong> Laisse les cheveux souples et légers.</li>
             <li><strong>Brillance Instantanée :</strong> Sublime l''éclat des cheveux.</li>
           </ul>
           <h3>Technologie Morpho-Keratine™ :</h3>
           <p>Gaine la fibre capillaire pour discipliner et lisser.</p>
           <h3>Conseils d''utilisation :</h3>
           <p>Vaporiser sur cheveux lavés et essorés. Répartir sur les longueurs et pointes. Procéder au coiffage. Sans rinçage.</p>
           <h3>Spécifications :</h3>
           <ul>
             <li><strong>Gamme :</strong> Discipline</li>
             <li><strong>Type de cheveux :</strong> Indisciplinés, Difficiles à coiffer, Frisottis</li>
             <li><strong>Contenance :</strong> 150ml</li>
           </ul>
           <p><small>*Test instrumental vs shampooing classique.</small></p>
         </div>',
        'Kérastase Discipline Spray Fluidissime', 'Kérastase Discipline Fluidissime Spray Anti-Frizz Thermo-Protecteur',
        'Disciplinez vos cheveux et contrôlez les frisottis avec le Spray Fluidissime de Kérastase.',
        'Kérastase, Discipline, Fluidissime, spray anti-frizz, thermo-protecteur, cheveux indisciplinés',
        'Spray Kérastase Discipline Fluidissime | Anti-Frizz', 'Anti-frizz 72h & Thermo-protection',
        'kerastase-discipline-fluidissime-spray', 'fr', 87)
on conflict (description_id) do nothing;

-- English Description (ID: 174)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (174, NOW(), NOW(),
        '<div>
           <h2>Kérastase Discipline Fluidissime Anti Frizz Spray</h2>
           <p>Anti-frizz heat-protecting spray for unruly hair. Controls frizz, protects from heat, and provides fluidity and shine.</p>
           <h3>Benefits:</h3>
           <ul>
             <li><strong>72h Anti-Frizz*:</strong> Long-lasting control of frizz and humidity.</li>
             <li><strong>Heat Protection:</strong> Protects from heat up to 230°C.</li>
             <li><strong>Fluidity & Movement:</strong> Leaves hair supple and light.</li>
             <li><strong>Instant Shine:</strong> Enhances hair radiance.</li>
           </ul>
           <h3>Morpho-Keratine™ Technology:</h3>
           <p>Coats the hair fiber to discipline and smooth.</p>
           <h3>How to Use:</h3>
           <p>Spray onto washed and towel-dried hair. Distribute through lengths and ends. Proceed to styling. Leave-in.</p>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Range:</strong> Discipline</li>
             <li><strong>Hair Type:</strong> Unruly, Difficult to style, Frizzy</li>
             <li><strong>Volume:</strong> 150ml</li>
           </ul>
           <p><small>*Instrumental test vs classical shampoo.</small></p>
         </div>',
        'Kérastase Discipline Fluidissime Spray', 'Kérastase Discipline Fluidissime Anti-Frizz Heat-Protecting Spray',
        'Discipline your hair and control frizz with Kérastase Fluidissime Spray.',
        'Kérastase, Discipline, Fluidissime, anti-frizz spray, heat protectant, unruly hair',
        'Spray Kérastase Discipline Fluidissime | Anti-Frizz', '72h Anti-frizz & Heat protection',
        'kerastase-discipline-fluidissime-spray-en', 'en', 87)
on conflict (description_id) do nothing;

-- Product 88: YSL Y Eau de Parfum (Manuf: 7, Type: 7)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (88, NOW(), NOW(), true, NOW(), false, 12.0,
        false, null, true, false, 0.4, 6.0, -- Approx for 60ml
        'REF-YSL-FRAG-YEDP88', 88, 7,
        '65f020632bc46470c104b76f', 7)
on conflict (product_id) do nothing;

-- French Description (ID: 175)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (175, NOW(), NOW(),
        '<div>
           <h2>Eau de Parfum Y Yves Saint Laurent</h2>
           <p>Y, le parfum masculin de la génération Y. L''incarnation de l''homme moderne, créatif et entrepreneur. Une fougère claire obscure intense.</p>
           <h3>Composition Olfactive :</h3>
           <p>La fraîcheur éclatante de la bergamote et du gingembre, intensifiée par un accord pomme verte. La sensualité audacieuse de la sauge et du genévrier, sublimée par l''élégance du bois de cèdre et de l''ambre gris.</p>
           <ul>
             <li><strong>Notes de Tête :</strong> Bergamote, Gingembre, Pomme</li>
             <li><strong>Notes de Cœur :</strong> Sauge, Genévrier, Géranium</li>
             <li><strong>Notes de Fond :</strong> Vétiver, Cèdre, Fève Tonka, Bois Ambrés</li>
           </ul>
           <h3>Le Flacon :</h3>
           <p>Un monolithe aux lignes épurées, coupé par un Y métallique signature au dos.</p>
           <h3>Spécifications :</h3>
           <ul>
             <li><strong>Type :</strong> Eau de Parfum</li>
             <li><strong>Famille Olfactive :</strong> Fougère Boisé</li>
             <li><strong>Contenance :</strong> 60ml (Exemple)</li>
           </ul>
         </div>',
        'YSL Y Eau de Parfum', 'Yves Saint Laurent Y Eau de Parfum - Parfum Fougère Homme',
        'Découvrez Y d''YSL, l''eau de parfum fougère boisée pour l''homme moderne et créatif.',
        'YSL, Y, eau de parfum, parfum homme, fougère, boisé, bergamote, sauge',
        'YSL Y Eau de Parfum | Fougère Boisé Moderne', 'Fougère claire obscure intense', 'ysl-y-eau-de-parfum', 'fr',
        88)
on conflict (description_id) do nothing;

-- English Description (ID: 176)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (176, NOW(), NOW(),
        '<div>
           <h2>Yves Saint Laurent Y Eau de Parfum</h2>
           <p>Y, the masculine fragrance of the Y generation. The embodiment of the modern man: creative and entrepreneurial. An intense light-dark fougère.</p>
           <h3>Olfactive Composition:</h3>
           <p>The bright freshness of bergamot and ginger, intensified by a green apple accord. The bold sensuality of sage and juniper, enhanced by the elegance of cedarwood and ambergris.</p>
           <ul>
             <li><strong>Top Notes:</strong> Bergamot, Ginger, Apple</li>
             <li><strong>Heart Notes:</strong> Sage, Juniper, Geranium</li>
             <li><strong>Base Notes:</strong> Vetiver, Cedar, Tonka Bean, Amber Woods</li>
           </ul>
           <h3>The Bottle:</h3>
           <p>A monolith with clean lines, cut by a signature metallic Y on the back.</p>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Type:</strong> Eau de Parfum</li>
             <li><strong>Olfactive Family:</strong> Woody Fougère</li>
             <li><strong>Volume:</strong> 60ml (Example)</li>
           </ul>
         </div>',
        'YSL Y Eau de Parfum', 'Yves Saint Laurent Y Eau de Parfum - Men''s Fougère Perfume',
        'Discover Y by YSL, the woody fougère eau de parfum for the modern and creative man.',
        'YSL, Y, eau de parfum, men perfume, fougère, woody, bergamot, sage',
        'YSL Y Eau de Parfum | Modern Woody Fougère', 'Intense light-dark fougère', 'ysl-y-eau-de-parfum-en', 'en', 88)
on conflict (description_id) do nothing;

-- Product 89: Guerlain L'Essentiel Natural Glow Foundation (Manuf: 8, Type: 6)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (89, NOW(), NOW(), true, NOW(), false, 11.0,
        false, null, true, false, 0.15, 4.5,
        'REF-GUER-MAKE-LESSENTIEL89', 89, 8,
        '65f020632bc46470c104b76f', 6)
on conflict (product_id) do nothing;

-- French Description (ID: 177)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (177, NOW(), NOW(),
        '<div>
           <h2>Fond de Teint L''Essentiel Éclat Naturel Guerlain</h2>
           <p>Le fond de teint qui allie maquillage et soin, avec 97% d''ingrédients d''origine naturelle*. Fini éclat naturel, tenue 16h**. Laisse la peau respirer.</p>
           <h3>Bénéfices :</h3>
           <ul>
             <li><strong>Éclat Naturel 16h** :</strong> Teint frais et lumineux toute la journée.</li>
             <li><strong>97% d''Origine Naturelle* :</strong> Formule soin qui améliore la peau jour après jour.</li>
             <li><strong>Couvrance Modulable :</strong> De légère à moyenne, pour unifier sans masquer.</li>
             <li><strong>Hydratation & Protection :</strong> Protège des agressions extérieures (lumière bleue, pollution). SPF 20.</li>
           </ul>
           <h3>Le Flacon :</h3>
           <p>Design unique et disruptif par Mathieu Lehanneur, inspiré par l''art subtil de l''équilibre.</p>
           <h3>Spécifications :</h3>
           <ul>
             <li><strong>Gamme :</strong> L''Essentiel</li>
             <li><strong>Fini :</strong> Éclat Naturel</li>
             <li><strong>Contenance :</strong> 30ml</li>
             <li><strong>Teinte :</strong> Large gamme (Exemple: 02N Clair)</li>
           </ul>
           <p><small>*Conformément à la norme ISO 16128.<br/>**Test instrumental réalisé par un laboratoire indépendant sur 20 femmes.</small></p>
         </div>',
        'Guerlain L''Essentiel Fond de Teint Éclat Naturel',
        'Guerlain L''Essentiel Natural Glow Foundation SPF 20 Tenue 16h',
        'Alliez maquillage et soin avec L''Essentiel de Guerlain, le fond de teint éclat naturel longue tenue.',
        'Guerlain, L''Essentiel, fond de teint, éclat naturel, origine naturelle, tenue 16h, SPF 20',
        'Fond de Teint Guerlain L''Essentiel | Éclat Naturel 16h', '97% d''ingrédients d''origine naturelle',
        'guerlain-lessentiel-natural-glow-foundation', 'fr', 89)
on conflict (description_id) do nothing;

-- English Description (ID: 178)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (178, NOW(), NOW(),
        '<div>
           <h2>Guerlain L''Essentiel Natural Glow Foundation</h2>
           <p>The foundation that combines makeup and skincare, with 97% naturally-derived ingredients*. Natural glow finish, 16-hour wear**. Lets skin breathe.</p>
           <h3>Benefits:</h3>
           <ul>
             <li><strong>16h Natural Glow**:</strong> Fresh, luminous complexion all day long.</li>
             <li><strong>97% Naturally-Derived Ingredients*:</strong> Skincare formula that improves skin day after day.</li>
             <li><strong>Buildable Coverage:</strong> Light to medium, evens out without masking.</li>
             <li><strong>Hydration & Protection:</strong> Protects from external aggressors (blue light, pollution). SPF 20.</li>
           </ul>
           <h3>The Bottle:</h3>
           <p>Unique and disruptive design by Mathieu Lehanneur, inspired by the subtle art of balance.</p>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Range:</strong> L''Essentiel</li>
             <li><strong>Finish:</strong> Natural Glow</li>
             <li><strong>Volume:</strong> 30ml</li>
             <li><strong>Shade:</strong> Wide range (Example: 02N Light)</li>
           </ul>
           <p><small>*In accordance with ISO 16128 standard.<br/>**Instrumental test conducted by an independent laboratory on 20 women.</small></p>
         </div>',
        'Guerlain L''Essentiel Natural Glow Foundation',
        'Guerlain L''Essentiel Natural Glow Foundation SPF 20 16h Wear',
        'Combine makeup and skincare with Guerlain L''Essentiel, the long-wearing natural glow foundation.',
        'Guerlain, L''Essentiel, foundation, natural glow, naturally-derived, 16h wear, SPF 20',
        'Foundation Guerlain L''Essentiel | Natural Glow 16h', '97% naturally-derived ingredients',
        'guerlain-lessentiel-natural-glow-foundation-en', 'en', 89)
on conflict (description_id) do nothing;

-- Product 90: Shiseido Waso Shikulime Gel-to-Oil Cleanser (Manuf: 9, Type: 5)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (90, NOW(), NOW(), true, NOW(), false, 16.0,
        false, null, true, false, 0.15, 5.0, -- Approx for 125ml tube
        'REF-SHIS-SKIN-WASOCLEAN90', 90, 9,
        '65f020632bc46470c104b76f', 5)
on conflict (product_id) do nothing;

-- French Description (ID: 179)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (179, NOW(), NOW(),
        '<div>
           <h2>Nettoyant Gel-en-Huile Shikulime Waso Shiseido</h2>
           <p>Un nettoyant démaquillant hybride gel-huile contenant du Shikuwasa Lime* qui élimine impuretés, excès de sébum et maquillage, tout en préservant l''hydratation naturelle de la peau.</p>
           <h3>Bénéfices :</h3>
           <ul>
             <li><strong>Nettoyage Efficace :</strong> Dissout même le maquillage waterproof sans dessécher.</li>
             <li><strong>Préserve l''Hydratation :</strong> Formule douce qui respecte la barrière cutanée.</li>
             <li><strong>Ingrédient Clé :</strong> Extrait de Shikuwasa (Citron Vert Japonais) pour soutenir la fonction barrière.</li>
             <li><strong>Texture Transformante :</strong> Gel frais qui se transforme en huile soyeuse au contact de la peau.</li>
           </ul>
           <h3>Formule Vegan & Clean :</h3>
           <p>Formulé sans ingrédients d''origine animale et selon la politique clean de Shiseido. Emballage durable.</p>
           <h3>Spécifications :</h3>
           <ul>
             <li><strong>Gamme :</strong> Waso</li>
             <li><strong>Type :</strong> Nettoyant Gel-en-Huile</li>
             <li><strong>Contenance :</strong> 125ml</li>
             <li><strong>Type de peau :</strong> Tous types</li>
           </ul>
           <p><small>*in vitro</small></p>
         </div>',
        'Shiseido Waso Shikulime Nettoyant Gel-en-Huile',
        'Shiseido Waso Shikulime Gel-to-Oil Cleanser Nettoyant Démaquillant',
        'Nettoyez et démaquillez votre peau en douceur avec le nettoyant gel-en-huile Shikulime Waso de Shiseido.',
        'Shiseido, Waso, Shikulime, nettoyant, démaquillant, gel-en-huile, peau propre, hydratation',
        'Nettoyant Shiseido Waso Shikulime | Gel-en-Huile', 'Nettoie sans dessécher',
        'shiseido-waso-shikulime-gel-to-oil-cleanser', 'fr', 90)
on conflict (description_id) do nothing;

-- English Description (ID: 180)
INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (180, NOW(), NOW(),
        '<div>
           <h2>Shiseido Waso Shikulime Gel-to-Oil Cleanser</h2>
           <p>A hybrid gel-oil makeup remover cleanser containing Shikuwasa Lime* that removes impurities, excess sebum, and makeup, while preserving the skin''s natural moisture.</p>
           <h3>Benefits:</h3>
           <ul>
             <li><strong>Effective Cleansing:</strong> Dissolves even waterproof makeup without drying.</li>
             <li><strong>Preserves Moisture:</strong> Gentle formula respects the skin barrier.</li>
             <li><strong>Key Ingredient:</strong> Shikuwasa (Japanese Lime) Extract to support barrier function.</li>
             <li><strong>Transforming Texture:</strong> Fresh gel transforms into a silky oil upon contact with skin.</li>
           </ul>
           <h3>Vegan & Clean Formula:</h3>
           <p>Formulated without animal-derived ingredients and following Shiseido''s clean policy. Sustainable packaging.</p>
           <h3>Specifications:</h3>
           <ul>
             <li><strong>Range:</strong> Waso</li>
             <li><strong>Type:</strong> Gel-to-Oil Cleanser</li>
             <li><strong>Volume:</strong> 125ml</li>
             <li><strong>Skin Type:</strong> All types</li>
           </ul>
           <p><small>*in vitro</small></p>
         </div>',
        'Shiseido Waso Shikulime Gel-to-Oil Cleanser', 'Shiseido Waso Shikulime Gel-to-Oil Cleanser Makeup Remover',
        'Gently cleanse and remove makeup with Shiseido Waso Shikulime gel-to-oil cleanser.',
        'Shiseido, Waso, Shikulime, cleanser, makeup remover, gel-to-oil, clean skin, hydration',
        'Cleanser Shiseido Waso Shikulime | Gel-to-Oil', 'Cleanses without drying',
        'shiseido-waso-shikulime-gel-to-oil-cleanser-en', 'en', 90)
on conflict (description_id) do nothing;