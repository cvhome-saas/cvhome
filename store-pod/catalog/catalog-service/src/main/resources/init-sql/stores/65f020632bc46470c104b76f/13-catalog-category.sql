/*
Generated SQL inserts similar to the provided template
where languages=['fr','en'] and store_id='65f020632bc46470c104b76f'
start category id from 13
start category_description from 25
generate 12 categories
domain for this store is beauté
Timestamps replaced with NOW()
Realistic beauty category names, codes, descriptions, meta info, etc. generated.
Hierarchy established: 4 parent categories (13-16), 8 child categories (17-24).
*/

-- Category 13: Skincare (Parent)
INSERT INTO catalog.category (category_id, date_created, date_modified, category_image, category_status, code,
                              depth, featured, lineage, sort_order, visible, store_merchant_id, parent_id)
VALUES (13, NOW(), NOW(), 'skincare_banner.jpg', true, 'SKINCARE', 0,
        true, '/13/', 0, true, '65f020632bc46470c104b76f', null)
on conflict (category_id) do nothing;

-- French Description (ID: 25)
INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (25, NOW(), NOW(), 'Explorez notre gamme complète de soins pour une peau saine et radieuse.', 'Soins de la Peau',
        'Soins de la Peau | Beauté Élégante Paris', 'Routine beauté essentielle',
        'Découvrez les meilleurs produits de soins de la peau : nettoyants, hydratants, sérums et plus encore pour tous types de peau.',
        'soins peau, hydratant, nettoyant, sérum, anti-âge, beauté',
        'Soins de la Peau Essentiels | Beauté Élégante Paris', 'soins-peau', 'fr', 13)
on conflict (description_id) do nothing;
-- English Description (ID: 26)
INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (26, NOW(), NOW(), 'Explore our complete range of skincare for healthy, radiant skin.', 'Skincare',
        'Skincare | Beauté Élégante Paris', 'Essential beauty routine',
        'Discover the best skincare products: cleansers, moisturizers, serums, and more for all skin types.',
        'skincare, moisturizer, cleanser, serum, anti-aging, beauty', 'Essential Skincare | Beauté Élégante Paris',
        'skincare', 'en', 13)
on conflict (description_id) do nothing;

-- Category 14: Makeup (Parent)
INSERT INTO catalog.category (category_id, date_created, date_modified, category_image, category_status, code,
                              depth, featured, lineage, sort_order, visible, store_merchant_id, parent_id)
VALUES (14, NOW(), NOW(), 'makeup_banner.jpg', true, 'MAKEUP', 0,
        true, '/14/', 1, true, '65f020632bc46470c104b76f', null)
on conflict (category_id) do nothing;

-- French Description (ID: 27)
INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (27, NOW(), NOW(), 'Sublimez votre beauté avec notre collection de maquillage tendance et de haute qualité.',
        'Maquillage', 'Maquillage | Beauté Élégante Paris', 'Exprimez votre style',
        'Trouvez tout pour votre maquillage : fond de teint, mascara, rouge à lèvres, fards à paupières des plus grandes marques.',
        'maquillage, fond de teint, mascara, rouge à lèvres, beauté, cosmétiques',
        'Collection Maquillage Tendance | Beauté Élégante Paris', 'maquillage', 'fr', 14)
on conflict (description_id) do nothing;
-- English Description (ID: 28)
INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (28, NOW(), NOW(), 'Enhance your beauty with our collection of trendy, high-quality makeup.', 'Makeup',
        'Makeup | Beauté Élégante Paris', 'Express your style',
        'Find everything for your makeup look: foundation, mascara, lipstick, eyeshadows from top brands.',
        'makeup, foundation, mascara, lipstick, beauty, cosmetics', 'Trendy Makeup Collection | Beauté Élégante Paris',
        'makeup', 'en', 14)
on conflict (description_id) do nothing;

-- Category 15: Fragrance (Parent)
INSERT INTO catalog.category (category_id, date_created, date_modified, category_image, category_status, code,
                              depth, featured, lineage, sort_order, visible, store_merchant_id, parent_id)
VALUES (15, NOW(), NOW(), 'fragrance_banner.jpg', true, 'FRAGRANCE', 0,
        true, '/15/', 2, true, '65f020632bc46470c104b76f', null)
on conflict (category_id) do nothing;

-- French Description (ID: 29)
INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (29, NOW(), NOW(), 'Découvrez notre sélection exquise de parfums pour homme et femme.', 'Parfums',
        'Parfums | Beauté Élégante Paris', 'Signatures olfactives uniques',
        'Explorez des parfums envoûtants, des eaux de toilette fraîches et des essences rares pour toutes les occasions.',
        'parfum, fragrance, eau de toilette, eau de parfum, homme, femme',
        'Parfums Exquis pour Homme et Femme | Beauté Élégante Paris', 'parfums', 'fr', 15)
on conflict (description_id) do nothing;
-- English Description (ID: 30)
INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (30, NOW(), NOW(), 'Discover our exquisite selection of fragrances for men and women.', 'Fragrance',
        'Fragrance | Beauté Élégante Paris', 'Unique olfactory signatures',
        'Explore captivating perfumes, fresh eaux de toilette, and rare essences for all occasions.',
        'perfume, fragrance, eau de toilette, eau de parfum, men, women',
        'Exquisite Fragrances for Men and Women | Beauté Élégante Paris', 'fragrance', 'en', 15)
on conflict (description_id) do nothing;

-- Category 16: Haircare (Parent)
INSERT INTO catalog.category (category_id, date_created, date_modified, category_image, category_status, code,
                              depth, featured, lineage, sort_order, visible, store_merchant_id, parent_id)
VALUES (16, NOW(), NOW(), 'haircare_banner.jpg', true, 'HAIRCARE', 0,
        true, '/16/', 3, true, '65f020632bc46470c104b76f', null)
on conflict (category_id) do nothing;

-- French Description (ID: 31)
INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (31, NOW(), NOW(), 'Prenez soin de vos cheveux avec nos produits capillaires professionnels.',
        'Soins Capillaires', 'Soins Capillaires | Beauté Élégante Paris', 'Cheveux sublimes et sains',
        'Shampoings, après-shampoings, masques, huiles et produits coiffants pour tous types de cheveux.',
        'soins cheveux, shampoing, après-shampoing, masque capillaire, coiffant',
        'Soins Capillaires Professionnels | Beauté Élégante Paris', 'soins-capillaires', 'fr', 16)
on conflict (description_id) do nothing;
-- English Description (ID: 32)
INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (32, NOW(), NOW(), 'Take care of your hair with our professional haircare products.', 'Haircare',
        'Haircare | Beauté Élégante Paris', 'Sublime and healthy hair',
        'Shampoos, conditioners, masks, oils, and styling products for all hair types.',
        'haircare, shampoo, conditioner, hair mask, styling', 'Professional Haircare | Beauté Élégante Paris',
        'haircare', 'en', 16)
on conflict (description_id) do nothing;

-- Category 17: Cleansers (Child of Skincare)
INSERT INTO catalog.category (category_id, date_created, date_modified, category_image, category_status, code,
                              depth, featured, lineage, sort_order, visible, store_merchant_id, parent_id)
VALUES (17, NOW(), NOW(), null, true, 'CLEANSERS', 1,
        false, '/13/17/', 0, true, '65f020632bc46470c104b76f', 13)
on conflict (category_id) do nothing;

-- French Description (ID: 33)
INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (33, NOW(), NOW(), 'Nettoyez votre peau en douceur avec nos nettoyants visage adaptés.', 'Nettoyants Visage',
        'Nettoyants Visage | Soins de la Peau', 'Peau propre et fraîche',
        'Gels, mousses, huiles et eaux micellaires pour éliminer impuretés et maquillage.',
        'nettoyant visage, gel nettoyant, eau micellaire, démaquillant',
        'Nettoyants Visage Doux et Efficaces | Beauté Élégante Paris', 'nettoyants-visage', 'fr', 17)
on conflict (description_id) do nothing;
-- English Description (ID: 34)
INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (34, NOW(), NOW(), 'Gently cleanse your skin with our suitable facial cleansers.', 'Facial Cleansers',
        'Facial Cleansers | Skincare', 'Clean and fresh skin',
        'Gels, foams, oils, and micellar waters to remove impurities and makeup.',
        'facial cleanser, cleansing gel, micellar water, makeup remover',
        'Gentle and Effective Facial Cleansers | Beauté Élégante Paris', 'facial-cleansers', 'en', 17)
on conflict (description_id) do nothing;

-- Category 18: Moisturizers (Child of Skincare)
INSERT INTO catalog.category (category_id, date_created, date_modified, category_image, category_status, code,
                              depth, featured, lineage, sort_order, visible, store_merchant_id, parent_id)
VALUES (18, NOW(), NOW(), null, true, 'MOISTURIZERS', 1,
        false, '/13/18/', 1, true, '65f020632bc46470c104b76f', 13)
on conflict (category_id) do nothing;

-- French Description (ID: 35)
INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (35, NOW(), NOW(), 'Hydratez et nourrissez votre peau avec nos crèmes et lotions hydratantes.', 'Hydratants',
        'Hydratants | Soins de la Peau', 'Hydratation intense',
        'Crèmes de jour, crèmes de nuit, lotions et fluides pour une peau douce et souple.',
        'hydratant, crème hydratante, lotion, soin hydratant', 'Crèmes et Lotions Hydratantes | Beauté Élégante Paris',
        'hydratants', 'fr', 18)
on conflict (description_id) do nothing;
-- English Description (ID: 36)
INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (36, NOW(), NOW(), 'Hydrate and nourish your skin with our moisturizing creams and lotions.', 'Moisturizers',
        'Moisturizers | Skincare', 'Intense hydration',
        'Day creams, night creams, lotions, and fluids for soft, supple skin.',
        'moisturizer, moisturizing cream, lotion, hydrating care',
        'Moisturizing Creams and Lotions | Beauté Élégante Paris', 'moisturizers', 'en', 18)
on conflict (description_id) do nothing;

-- Category 19: Face Makeup (Child of Makeup)
INSERT INTO catalog.category (category_id, date_created, date_modified, category_image, category_status, code,
                              depth, featured, lineage, sort_order, visible, store_merchant_id, parent_id)
VALUES (19, NOW(), NOW(), null, true, 'FACE_MAKEUP', 1,
        false, '/14/19/', 0, true, '65f020632bc46470c104b76f', 14)
on conflict (category_id) do nothing;

-- French Description (ID: 37)
INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (37, NOW(), NOW(), 'Unifiez et sublimez votre teint avec nos produits de maquillage pour le visage.',
        'Maquillage Teint', 'Maquillage Teint | Maquillage', 'Teint parfait zéro défaut',
        'Fonds de teint, correcteurs, poudres, blushs et enlumineurs pour un teint éclatant.',
        'maquillage teint, fond de teint, correcteur, poudre, blush',
        'Maquillage pour un Teint Parfait | Beauté Élégante Paris', 'maquillage-teint', 'fr', 19)
on conflict (description_id) do nothing;
-- English Description (ID: 38)
INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (38, NOW(), NOW(), 'Unify and enhance your complexion with our face makeup products.', 'Face Makeup',
        'Face Makeup | Makeup', 'Flawless perfect complexion',
        'Foundations, concealers, powders, blushes, and highlighters for a radiant complexion.',
        'face makeup, foundation, concealer, powder, blush', 'Makeup for a Perfect Complexion | Beauté Élégante Paris',
        'face-makeup', 'en', 19)
on conflict (description_id) do nothing;

-- Category 20: Eye Makeup (Child of Makeup)
INSERT INTO catalog.category (category_id, date_created, date_modified, category_image, category_status, code,
                              depth, featured, lineage, sort_order, visible, store_merchant_id, parent_id)
VALUES (20, NOW(), NOW(), null, true, 'EYE_MAKEUP', 1,
        false, '/14/20/', 1, true, '65f020632bc46470c104b76f', 14)
on conflict (category_id) do nothing;

-- French Description (ID: 39)
INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (39, NOW(), NOW(), 'Intensifiez votre regard avec notre sélection de maquillage pour les yeux.',
        'Maquillage Yeux', 'Maquillage Yeux | Maquillage', 'Regard captivant',
        'Mascaras, eyeliners, fards à paupières et crayons pour sublimer vos yeux.',
        'maquillage yeux, mascara, eyeliner, fard à paupières, crayon yeux',
        'Maquillage pour les Yeux | Beauté Élégante Paris', 'maquillage-yeux', 'fr', 20)
on conflict (description_id) do nothing;
-- English Description (ID: 40)
INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (40, NOW(), NOW(), 'Intensify your gaze with our selection of eye makeup.', 'Eye Makeup', 'Eye Makeup | Makeup',
        'Captivating look', 'Mascaras, eyeliners, eyeshadows, and pencils to enhance your eyes.',
        'eye makeup, mascara, eyeliner, eyeshadow, eye pencil', 'Eye Makeup | Beauté Élégante Paris', 'eye-makeup',
        'en', 20)
on conflict (description_id) do nothing;

-- Category 21: Women's Fragrance (Child of Fragrance)
INSERT INTO catalog.category (category_id, date_created, date_modified, category_image, category_status, code,
                              depth, featured, lineage, sort_order, visible, store_merchant_id, parent_id)
VALUES (21, NOW(), NOW(), null, true, 'WOMEN_FRAGRANCE', 1,
        false, '/15/21/', 0, true, '65f020632bc46470c104b76f', 15)
on conflict (category_id) do nothing;

-- French Description (ID: 41)
INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (41, NOW(), NOW(), 'Trouvez votre signature olfactive parmi nos parfums féminins.', 'Parfums Femme',
        'Parfums Femme | Parfums', 'Essences féminines',
        'Eaux de parfum, eaux de toilette et extraits floraux, orientaux ou frais pour elle.',
        'parfum femme, eau de parfum femme, fragrance féminine', 'Parfums pour Femme | Beauté Élégante Paris',
        'parfums-femme', 'fr', 21)
on conflict (description_id) do nothing;
-- English Description (ID: 42)
INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (42, NOW(), NOW(), 'Find your signature scent among our women''s fragrances.', 'Women''s Fragrance',
        'Women''s Fragrance | Fragrance', 'Feminine essences',
        'Eaux de parfum, eaux de toilette, and floral, oriental, or fresh extracts for her.',
        'women perfume, women eau de parfum, feminine fragrance', 'Fragrances for Women | Beauté Élégante Paris',
        'womens-fragrance', 'en', 21)
on conflict (description_id) do nothing;

-- Category 22: Men's Fragrance (Child of Fragrance)
INSERT INTO catalog.category (category_id, date_created, date_modified, category_image, category_status, code,
                              depth, featured, lineage, sort_order, visible, store_merchant_id, parent_id)
VALUES (22, NOW(), NOW(), null, true, 'MEN_FRAGRANCE', 1,
        false, '/15/22/', 1, true, '65f020632bc46470c104b76f', 15)
on conflict (category_id) do nothing;

-- French Description (ID: 43)
INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (43, NOW(), NOW(), 'Explorez notre collection de parfums masculins élégants et audacieux.', 'Parfums Homme',
        'Parfums Homme | Parfums', 'Caractère masculin',
        'Eaux de toilette, eaux de parfum et colognes boisées, épicées ou aquatiques pour lui.',
        'parfum homme, eau de toilette homme, fragrance masculine', 'Parfums pour Homme | Beauté Élégante Paris',
        'parfums-homme', 'fr', 22)
on conflict (description_id) do nothing;
-- English Description (ID: 44)
INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (44, NOW(), NOW(), 'Explore our collection of elegant and bold men''s fragrances.', 'Men''s Fragrance',
        'Men''s Fragrance | Fragrance', 'Masculine character',
        'Eaux de toilette, eaux de parfum, and woody, spicy, or aquatic colognes for him.',
        'men perfume, men eau de toilette, masculine fragrance', 'Fragrances for Men | Beauté Élégante Paris',
        'mens-fragrance', 'en', 22)
on conflict (description_id) do nothing;

-- Category 23: Shampoo (Child of Haircare)
INSERT INTO catalog.category (category_id, date_created, date_modified, category_image, category_status, code,
                              depth, featured, lineage, sort_order, visible, store_merchant_id, parent_id)
VALUES (23, NOW(), NOW(), null, true, 'SHAMPOO', 1,
        false, '/16/23/', 0, true, '65f020632bc46470c104b76f', 16)
on conflict (category_id) do nothing;

-- French Description (ID: 45)
INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (45, NOW(), NOW(), 'Nettoyez et traitez vos cheveux avec nos shampoings spécifiques.', 'Shampoings',
        'Shampoings | Soins Capillaires', 'Nettoyage expert',
        'Shampoings hydratants, réparateurs, volumateurs ou pour cheveux colorés.',
        'shampoing, soin lavant, cheveux gras, cheveux secs, cheveux colorés',
        'Shampoings pour Tous Types de Cheveux | Beauté Élégante Paris', 'shampoings', 'fr', 23)
on conflict (description_id) do nothing;
-- English Description (ID: 46)
INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (46, NOW(), NOW(), 'Cleanse and treat your hair with our specific shampoos.', 'Shampoos', 'Shampoos | Haircare',
        'Expert cleansing', 'Hydrating, repairing, volumizing, or color-treated hair shampoos.',
        'shampoo, cleansing care, oily hair, dry hair, colored hair',
        'Shampoos for All Hair Types | Beauté Élégante Paris', 'shampoos', 'en', 23)
on conflict (description_id) do nothing;

-- Category 24: Conditioner (Child of Haircare)
INSERT INTO catalog.category (category_id, date_created, date_modified, category_image, category_status, code,
                              depth, featured, lineage, sort_order, visible, store_merchant_id, parent_id)
VALUES (24, NOW(), NOW(), null, true, 'CONDITIONER', 1,
        false, '/16/24/', 1, true, '65f020632bc46470c104b76f', 16)
on conflict (category_id) do nothing;

-- French Description (ID: 47)
INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (47, NOW(), NOW(), 'Démêlez, nourrissez et protégez vos cheveux avec nos après-shampoings.', 'Après-Shampoings',
        'Après-Shampoings | Soins Capillaires', 'Douceur et brillance',
        'Après-shampoings pour hydrater, réparer, lisser ou protéger la couleur.',
        'après-shampoing, conditionneur, soin démêlant, cheveux abîmés',
        'Après-Shampoings Nourrissants et Protecteurs | Beauté Élégante Paris', 'apres-shampoings', 'fr', 24)
on conflict (description_id) do nothing;
-- English Description (ID: 48)
INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (48, NOW(), NOW(), 'Detangle, nourish, and protect your hair with our conditioners.', 'Conditioners',
        'Conditioners | Haircare', 'Softness and shine', 'Conditioners to hydrate, repair, smooth, or protect color.',
        'conditioner, detangling care, damaged hair', 'Nourishing and Protective Conditioners | Beauté Élégante Paris',
        'conditioners', 'en', 24)
on conflict (description_id) do nothing;