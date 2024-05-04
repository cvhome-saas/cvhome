drop schema IF EXISTS store cascade;
create schema if not exists store;
create table if not exists store.currency
(
    currency_id            bigint     not null primary key,
    currency_code          varchar(255)
        constraint uk_7n17ygajjchsso2n0lyxrsyif unique,
    currency_currency_code varchar(3) not null
        constraint uk_166n2gj29i7mri2bgrjm8hqy8 unique,
    currency_name          varchar(255)
        constraint uk_ntgaxtcgi6crpijka4yu7927o unique,
    currency_supported     boolean
);
create table if not exists store.geozone
(
    geozone_id   bigint not null primary key,
    geozone_code varchar(255),
    geozone_name varchar(255)
);
create table if not exists store.country
(
    country_id        integer      not null primary key,
    country_isocode   varchar(255) not null
        constraint uk_ojpfhnup1d46c2gtjfn0kdptw unique,
    country_supported boolean,
    geozone_id        bigint
        constraint fkdkldrhiqa274imykuh0p8icb references store.geozone
);
create table if not exists store.language
(
    language_id   integer      not null primary key,
    date_created  timestamp(6),
    date_modified timestamp(6),
    updt_id       varchar(60),
    code          varchar(255) not null,
    sort_order    integer
);
create table if not exists store.country_description
(
    description_id bigint       not null primary key,
    date_created   timestamp(6),
    date_modified  timestamp(6),
    updt_id        varchar(60),
    description    text,
    name           varchar(120) not null,
    title          varchar(100),
    language_id    integer      not null
        constraint fkorknwk5v8t484n6goauqlw2jj references store.language,
    country_id     integer      not null
        constraint fkcqt2eksj1ucfg822rlx0ah55m references store.country,
    constraint uklkb1vgcxje49l1ky7n8qt1wk8 unique (country_id, language_id)
);
create table if not exists store.geozone_description
(
    description_id bigint       not null primary key,
    date_created   timestamp(6),
    date_modified  timestamp(6),
    updt_id        varchar(60),
    description    text,
    name           varchar(120) not null,
    title          varchar(100),
    language_id    integer      not null
        constraint fkb5ti8ksr90esthy3knbjut8n1 references store.language,
    geozone_id     bigint
        constraint fkmq8egxrpyx60s17ng3kbx80ex references store.geozone,
    constraint uk9c0pjwa1uruwhqnv6x7rb4x1j unique (geozone_id, language_id)
);
create index if not exists code_idx2 on store.language (code);
create table if not exists store.module_configuration
(
    module_conf_id bigint       not null primary key,
    date_created   timestamp(6),
    date_modified  timestamp(6),
    updt_id        varchar(60),
    code           varchar(255) not null,
    details        text,
    configuration  varchar(4000),
    custom_ind     boolean,
    image          varchar(255),
    module         varchar(255),
    regions        varchar(255),
    type           varchar(255)
);
create index if not exists module_configuration_module on store.module_configuration (module);
create table if not exists store.permission
(
    permission_id   integer not null primary key,
    date_created    timestamp(6),
    date_modified   timestamp(6),
    updt_id         varchar(60),
    permission_name varchar(255)
        constraint uk_l3pmqryh8vgle52647itattb9 unique
);
create table if not exists store.sm_group
(
    group_id      integer not null primary key,
    date_created  timestamp(6),
    date_modified timestamp(6),
    updt_id       varchar(60),
    group_name    varchar(255)
        constraint uk_5yavgmoqcvpwodpntbt8046ib unique,
    group_type    varchar(255)
        constraint sm_group_group_type_check check (
            (group_type):: text = ANY (
                (
                    ARRAY [ 'ADMIN' :: character varying,
                        'CUSTOMER' :: character varying]
                    ):: text[]
                )
            )
);
create table if not exists store.permission_group
(
    group_id      integer not null
        constraint fkono3u9yciwi4w10tu2xf2gvxg references store.sm_group,
    permission_id integer not null
        constraint fkcn775l61gpimtyac31up3vbg5 references store.permission,
    primary key (group_id, permission_id)
);
create index if not exists sm_group_group_type on store.sm_group (group_type);
create table if not exists store.sm_sequencer
(
    seq_name  varchar(255) not null primary key,
    seq_count bigint
);
create table if not exists store.system_configuration
(
    system_config_id bigint not null primary key,
    date_created     timestamp(6),
    date_modified    timestamp(6),
    updt_id          varchar(60),
    config_key       varchar(255),
    value            varchar(255)
);
create table if not exists store.zone
(
    zone_id    bigint       not null primary key,
    zone_code  varchar(255) not null
        constraint uk_sdhgvsfolxelu1uw6157vvt17 unique,
    country_id integer      not null
        constraint fkjd0ph7dy8trl6m0lyid2eicsb references store.country
);
create table if not exists store.merchant_store
(
    merchant_id              integer      not null primary key,
    date_created             timestamp(6),
    date_modified            timestamp(6),
    updt_id                  varchar(60),
    store_code               varchar(100) not null
        constraint uk_acooxdls88ynobig2dpu6whum unique,
    continueshoppingurl      varchar(150),
    currency_format_national boolean,
    domain_name              varchar(80),
    in_business_since        date,
    invoice_template         varchar(25),
    lineage                  varchar(255),
    org                      varchar(255) not null,
    is_retailer              boolean,
    seizeunitcode            varchar(5),
    store_email              varchar(60)  not null,
    store_logo               varchar(100),
    store_banner             varchar(100),
    store_template           varchar(25),
    store_address            varchar(255),
    store_city               varchar(100),
    store_name               varchar(100) not null,
    store_phone              varchar(50),
    store_postal_code        varchar(15),
    store_state_prov         varchar(100),
    use_cache                boolean,
    weightunitcode           varchar(5),
    country_id               integer      not null
        constraint fki8ejnku0mrte0hku5i1lpcjt8 references store.country,
    currency_id              bigint       not null
        constraint fks3ilgl79s6f9rcsgate0vmw1c references store.currency,
    language_id              integer      not null
        constraint fkoc6wtwuhiwk6lc2q677bl7e5w references store.language,
    parent_id                integer
        constraint fk66mkgbwobrqrkeo0iatcm3dib references store.merchant_store,
    zone_id                  bigint
        constraint fk6rwca37ioi3tqpfxupmuyrgd0 references store.zone
);
create table if not exists store.catalog
(
    id              bigint       not null primary key,
    date_created    timestamp(6),
    date_modified   timestamp(6),
    updt_id         varchar(60),
    code            varchar(100) not null,
    default_catalog boolean,
    sort_order      integer,
    visible         boolean,
    merchant_id     integer      not null
        constraint fkny9nen8lswy93m0kc1mg3bcln references store.merchant_store,
    constraint ukhfho9x40s3iyf543shh3qhwmp unique (merchant_id, code)
);
create table if not exists store.category
(
    category_id     bigint       not null primary key,
    date_created    timestamp(6),
    date_modified   timestamp(6),
    updt_id         varchar(60),
    category_image  varchar(100),
    category_status boolean,
    code            varchar(100) not null,
    depth           integer,
    featured        boolean,
    lineage         varchar(255),
    sort_order      integer,
    visible         boolean,
    merchant_id     integer      not null
        constraint fk4chm5kmqglsek38bmj82xtcvp references store.merchant_store,
    parent_id       bigint
        constraint fk2y94svpmqttx80mshyny85wqr references store.category,
    constraint uk43eimtph12w3in686ufqyrfen unique (merchant_id, code)
);
create table if not exists store.catalog_entry
(
    id            bigint not null primary key,
    date_created  timestamp(6),
    date_modified timestamp(6),
    updt_id       varchar(60),
    visible       boolean,
    catalog_id    bigint not null
        constraint fk5l3vrt6m79m06vyp4b290835p references store.catalog,
    category_id   bigint not null
        constraint fkhxjb7sm6l0fd0fpy2r0tcil28 references store.category,
    constraint uk43dn30yr2wsmj1det6vqoxtfw unique (category_id, catalog_id)
);
create index if not exists idxiekj8rpfx83k5nww1flbu7tpb on store.category (lineage);
create table if not exists store.category_description
(
    description_id     bigint       not null primary key,
    date_created       timestamp(6),
    date_modified      timestamp(6),
    updt_id            varchar(60),
    description        text,
    name               varchar(120) not null,
    title              varchar(100),
    category_highlight varchar(255),
    meta_description   varchar(255),
    meta_keywords      varchar(255),
    meta_title         varchar(120),
    sef_url            varchar(120),
    language_id        integer      not null
        constraint fktbbtihri94lo8wwou6gs9acrt references store.language,
    category_id        bigint       not null
        constraint fkcf1yvvfw0o7fvhxpryuetekcb references store.category,
    constraint ukj86xg9q2umw8u8et9sbfbsph5 unique (category_id, language_id)
);
create table if not exists store.content
(
    content_id       bigint       not null primary key,
    date_created     timestamp(6),
    date_modified    timestamp(6),
    updt_id          varchar(60),
    code             varchar(100) not null,
    content_position varchar(10)
        constraint content_content_position_check check (
            (content_position):: text = ANY (
                (
                    ARRAY [ 'LEFT' :: character varying,
                        'RIGHT' :: character varying]
                    ):: text[]
                )
            ),
    content_type     varchar(10)
        constraint content_content_type_check check (
            (content_type):: text = ANY (
                (
                    ARRAY [ 'BOX' :: character varying, 'PAGE' :: character varying,
                        'SECTION' :: character varying]
                    ):: text[]
                )
            ),
    link_to_menu     boolean,
    product_group    varchar(255),
    sort_order       integer,
    visible          boolean,
    merchant_id      integer      not null
        constraint fkkhljxdts61bnfuhwmn2oj8wbi references store.merchant_store,
    constraint ukarp2mu6w0ldc2fsbsvrr2h2oq unique (merchant_id, code)
);
create index if not exists code_idx on store.content (code);
create table if not exists store.content_description
(
    description_id   bigint       not null primary key,
    date_created     timestamp(6),
    date_modified    timestamp(6),
    updt_id          varchar(60),
    description      text,
    name             varchar(120) not null,
    title            varchar(100),
    meta_description varchar(255),
    meta_keywords    varchar(255),
    meta_title       varchar(255),
    sef_url          varchar(120),
    language_id      integer      not null
        constraint fkbfq2ntr9ebqrdeueuy94gordd references store.language,
    content_id       bigint       not null
        constraint fklu77slp5yqo5p281lk6vdpppi references store.content,
    constraint ukfrkqw8gr1kexiuv0h76s6xvpr unique (content_id, language_id)
);
create table if not exists store.customer
(
    customer_id             bigint      not null primary key,
    customer_anonymous      boolean,
    date_created            timestamp(6),
    date_modified           timestamp(6),
    updt_id                 varchar(60),
    billing_street_address  varchar(256),
    billing_city            varchar(100),
    billing_company         varchar(100),
    billing_first_name      varchar(64) not null,
    billing_last_name       varchar(64) not null,
    latitude                varchar(100),
    longitude               varchar(100),
    billing_postcode        varchar(20),
    billing_state           varchar(100),
    billing_telephone       varchar(32),
    customer_company        varchar(100),
    reset_credentials_req   varchar(256),
    reset_credentials_exp   date,
    review_avg              numeric(38, 2),
    review_count            integer,
    customer_dob            timestamp(6),
    delivery_street_address varchar(256),
    delivery_city           varchar(100),
    delivery_company        varchar(100),
    delivery_first_name     varchar(64),
    delivery_last_name      varchar(64),
    delivery_postcode       varchar(20),
    delivery_state          varchar(100),
    delivery_telephone      varchar(32),
    customer_email_address  varchar(96) not null,
    customer_gender         char
        constraint customer_customer_gender_check check (
            customer_gender = ANY (ARRAY [ 'M' :: bpchar, 'F' :: bpchar])
            ),
    customer_nick           varchar(96),
    customer_password       varchar(60),
    provider                varchar(255),
    billing_country_id      integer     not null
        constraint fkfaowmep2brbb0f973vnuw9cr3 references store.country,
    billing_zone_id         bigint
        constraint fkduvvfcfgokno59eki42x1biuq references store.zone,
    language_id             integer     not null
        constraint fkptjfm48g49mrkhravgibb02h7 references store.language,
    delivery_country_id     integer
        constraint fk6cwqotnrr8rh9hrw0sn1kt1e6 references store.country,
    delivery_zone_id        bigint
        constraint fk8yqdxfoqy25vvy56gs68k9p4 references store.zone,
    merchant_id             integer     not null
        constraint fkjmp2kgdhcnqs121y0wfh82pdw references store.merchant_store,
    constraint uk6s60vvprfv9dyjjet9tfqvor0 unique (merchant_id, customer_nick)
);
create table if not exists store.customer_group
(
    customer_id bigint  not null
        constraint fkbopjkmu9mriivehbk9yd6rbvw references store.customer,
    group_id    integer not null
        constraint fk37ejf8g2duw45e5lopd3l8w6p references store.sm_group
);
create table if not exists store.customer_option
(
    customer_option_id   bigint  not null primary key,
    customer_opt_active  boolean,
    customer_opt_code    varchar(255),
    customer_option_type varchar(10),
    customer_opt_public  boolean,
    sort_order           integer,
    merchant_id          integer not null
        constraint fkdum9l2iu04mak11vja3rik25s references store.merchant_store,
    constraint uk3b7r9dspg90juo2u8iu3e45bn unique (merchant_id, customer_opt_code)
);
create index if not exists cust_opt_code_idx on store.customer_option (customer_opt_code);
create table if not exists store.customer_option_desc
(
    description_id          bigint       not null primary key,
    date_created            timestamp(6),
    date_modified           timestamp(6),
    updt_id                 varchar(60),
    description             text,
    name                    varchar(120) not null,
    title                   varchar(100),
    customer_option_comment varchar(4000),
    language_id             integer      not null
        constraint fkhalrtqiqm06t2s17lwnu2icos references store.language,
    customer_option_id      bigint       not null
        constraint fk201v4egnnqx20q6qky676mks2 references store.customer_option,
    constraint uk2mhxxxmla4klhbm68vuhectmn unique (customer_option_id, language_id)
);
create table if not exists store.customer_option_value
(
    customer_option_value_id bigint  not null primary key,
    customer_opt_val_code    varchar(255),
    customer_opt_val_image   varchar(255),
    sort_order               integer,
    merchant_id              integer not null
        constraint fkbko1uy19v04ih6kxqfmll6qam references store.merchant_store,
    constraint uksukgvqnjcdoybbfq9gm8abavu unique (
                                                   merchant_id, customer_opt_val_code
        )
);
create table if not exists store.customer_attribute
(
    customer_attribute_id bigint not null primary key,
    customer_attr_txt_val varchar(255),
    customer_id           bigint not null
        constraint fkkt9jyeddekdvrhcx806k7os0g references store.customer,
    option_id             bigint not null
        constraint fkm7j9jcewyjmeh5ai9nsuuvi8i references store.customer_option,
    option_value_id       bigint not null
        constraint fksipgdm09ffity5b9g5c50jx3w references store.customer_option_value,
    constraint uksoy0ia28b7ehe2rru8c5q5v87 unique (option_id, customer_id)
);
create table if not exists store.customer_opt_val_description
(
    description_id      bigint       not null primary key,
    date_created        timestamp(6),
    date_modified       timestamp(6),
    updt_id             varchar(60),
    description         text,
    name                varchar(120) not null,
    title               varchar(100),
    language_id         integer      not null
        constraint fkigy69gv3asw6r90hno6vc3vu4 references store.language,
    customer_opt_val_id bigint
        constraint fkmej30yqti7y4t4iqsq15t9yc3 references store.customer_option_value,
    constraint ukc9ph5ry410o66gc1rwnd648lr unique (
                                                   customer_opt_val_id, language_id
        )
);
create table if not exists store.customer_option_set
(
    customer_optionset_id    bigint not null primary key,
    sort_order               integer,
    customer_option_id       bigint not null
        constraint fkiuve99ti5qr9j3qvth1yujjmh references store.customer_option,
    customer_option_value_id bigint not null
        constraint fko2ohdbcnl065c2u3icfya9688 references store.customer_option_value,
    constraint uk1lqrcukylq7x2te82cai3t7y9 unique (
                                                   customer_option_id, customer_option_value_id
        )
);
create index if not exists cust_opt_val_code_idx on store.customer_option_value (customer_opt_val_code);
create table if not exists store.customer_review
(
    customer_review_id   bigint not null primary key,
    date_created         timestamp(6),
    date_modified        timestamp(6),
    updt_id              varchar(60),
    review_date          timestamp(6),
    reviews_rating       double precision,
    reviews_read         bigint,
    status               integer,
    customers_id         bigint
        constraint fktnsb170ewuhjtok3p50nuaby2 references store.customer,
    reviewed_customer_id bigint
        constraint uk_p329f4tc2gt8e9iicefcf1dwu unique
        constraint fkrt9to366jismmfdap0onydy9c references store.customer,
    constraint ukpe13frashysxlaqa4rcms49j6 unique (
                                                   customers_id, reviewed_customer_id
        )
);
create table if not exists store.customer_review_description
(
    description_id     bigint       not null primary key,
    date_created       timestamp(6),
    date_modified      timestamp(6),
    updt_id            varchar(60),
    description        text,
    name               varchar(120) not null,
    title              varchar(100),
    language_id        integer      not null
        constraint fkellhkha3lu8w2b5kuuujj5d38 references store.language,
    customer_review_id bigint
        constraint fk3nu9inejlfrkcig7ppv3glhrh references store.customer_review,
    constraint uk70jse47ia0srsp2t9p97nrpp1 unique (customer_review_id, language_id)
);
create table if not exists store.file_history
(
    file_history_id bigint       not null primary key,
    accounted_date  timestamp(6),
    date_added      timestamp(6) not null,
    date_deleted    timestamp(6),
    download_count  integer      not null,
    file_id         bigint,
    filesize        integer      not null,
    merchant_id     integer      not null
        constraint fk5af1oi3bg5513rvlo5le3kn7w references store.merchant_store,
    constraint ukrd7na1n326tk1wbqnd2v77xfe unique (merchant_id, file_id)
);
create table if not exists store.manufacturer
(
    manufacturer_id    bigint       not null primary key,
    date_created       timestamp(6),
    date_modified      timestamp(6),
    updt_id            varchar(60),
    code               varchar(100) not null,
    manufacturer_image varchar(255),
    sort_order         integer,
    merchant_id        integer      not null
        constraint fki7iwd1i0u4djj3g8xrohy7lc2 references store.merchant_store,
    constraint ukp2mxknv7k8tgyovc42u5ohasj unique (merchant_id, code)
);
create table if not exists store.manufacturer_description
(
    description_id    bigint       not null primary key,
    date_created      timestamp(6),
    date_modified     timestamp(6),
    updt_id           varchar(60),
    description       text,
    name              varchar(120) not null,
    title             varchar(100),
    date_last_click   timestamp(6),
    manufacturers_url varchar(255),
    url_clicked       integer,
    language_id       integer      not null
        constraint fk3afpqu1x4cmhbrux611kpvu5i references store.language,
    manufacturer_id   bigint       not null
        constraint fk2cpxn0kaionj660yaqdln4sfi references store.manufacturer,
    constraint ukn2xody8lg6steenheu0anried unique (manufacturer_id, language_id)
);
create table if not exists store.merchant_configuration
(
    merchant_config_id bigint not null primary key,
    active             boolean,
    date_created       timestamp(6),
    date_modified      timestamp(6),
    updt_id            varchar(60),
    config_key         varchar(255),
    type               varchar(255)
        constraint merchant_configuration_type_check check (
            (type):: text = ANY (
                (
                    ARRAY [ 'INTEGRATION' :: character varying,
                        'SHOP' :: character varying, 'CONFIG' :: character varying,
                        'SOCIAL' :: character varying]
                    ):: text[]
                )
            ),
    value              text,
    merchant_id        integer
        constraint fkfe4kl75tt58f3p01ma990jk2n references store.merchant_store,
    constraint ukq3kc48q2c3fmku9qb12k91b27 unique (merchant_id, config_key)
);
create table if not exists store.merchant_language
(
    stores_merchant_id    integer not null
        constraint fkj2fld4p6gttsjajxd4eul3fyj references store.merchant_store,
    languages_language_id integer not null
        constraint fk9rwrfjsumvmq3dnnl5at4whfw references store.language
);
create table if not exists store.merchant_log
(
    merchant_log_id bigint  not null primary key,
    log             text,
    module          varchar(25),
    merchant_id     integer not null
        constraint fk2sbquy58b7aw2s0hf47531db1 references store.merchant_store
);
create index if not exists idxgoqgwut6gadxj9fhox44xwwt1 on store.merchant_store (lineage);
create table if not exists store.optin
(
    optin_id    bigint       not null primary key,
    code        varchar(255) not null,
    description varchar(255),
    end_date    timestamp(6),
    type        varchar(255) not null
        constraint optin_type_check check (
            (type):: text = ANY (
                (
                    ARRAY [ 'NEWSLETTER' :: character varying,
                        'PROMOTIONS' :: character varying]
                    ):: text[]
                )
            ),
    start_date  timestamp(6),
    merchant_id integer
        constraint fkbwy7klvqr4nj5wws0lp6qeyit references store.merchant_store,
    constraint uk38ltbua8g8pplg8k4w0jxnh69 unique (merchant_id, code)
);
create table if not exists store.customer_optin
(
    customer_optin_id bigint       not null primary key,
    email             varchar(255) not null,
    first             varchar(255),
    last              varchar(255),
    optin_date        timestamp(6),
    value             text,
    merchant_id       integer      not null
        constraint fkr9yh0yi3o1r2pf3ol642ei6gj references store.merchant_store,
    optin_id          bigint
        constraint fkr1uwied13q1bdptgap3vdu8xb references store.optin,
    constraint ukmaibabrcxys6ij7ifc1iqggb unique (email, optin_id)
);
create table if not exists store.orders
(
    order_id                bigint      not null primary key,
    billing_street_address  varchar(256),
    billing_city            varchar(100),
    billing_company         varchar(100),
    billing_first_name      varchar(64) not null,
    billing_last_name       varchar(64) not null,
    latitude                varchar(100),
    longitude               varchar(100),
    billing_postcode        varchar(20),
    billing_state           varchar(100),
    billing_telephone       varchar(32),
    channel                 varchar(255)
        constraint orders_channel_check check (
            (channel):: text = ANY (
                (
                    ARRAY [ 'ONLINE' :: character varying,
                        'API' :: character varying]
                    ):: text[]
                )
            ),
    confirmed_address       boolean,
    card_type               varchar(255)
        constraint orders_card_type_check check (
            (card_type):: text = ANY (
                (
                    ARRAY [ 'AMEX' :: character varying,
                        'VISA' :: character varying, 'MASTERCARD' :: character varying,
                        'DINERS' :: character varying, 'DISCOVERY' :: character varying]
                    ):: text[]
                )
            ),
    cc_cvv                  varchar(255),
    cc_expires              varchar(255),
    cc_number               varchar(255),
    cc_owner                varchar(255),
    currency_value          numeric(38, 2),
    customer_agreed         boolean,
    customer_email_address  varchar(50) not null,
    customer_id             bigint,
    date_purchased          date,
    delivery_street_address varchar(256),
    delivery_city           varchar(100),
    delivery_company        varchar(100),
    delivery_first_name     varchar(64),
    delivery_last_name      varchar(64),
    delivery_postcode       varchar(20),
    delivery_state          varchar(100),
    delivery_telephone      varchar(32),
    ip_address              varchar(255),
    last_modified           timestamp(6),
    locale                  varchar(255),
    order_date_finished     timestamp(6),
    order_type              varchar(255)
        constraint orders_order_type_check check (
            (order_type):: text = ANY (
                (
                    ARRAY [ 'ORDER' :: character varying,
                        'BOOKING' :: character varying]
                    ):: text[]
                )
            ),
    payment_module_code     varchar(255),
    payment_type            varchar(255)
        constraint orders_payment_type_check check (
            (payment_type):: text = ANY (
                (
                    ARRAY [ 'CREDITCARD' :: character varying,
                        'FREE' :: character varying, 'COD' :: character varying,
                        'MONEYORDER' :: character varying,
                        'PAYPAL' :: character varying, 'INVOICE' :: character varying,
                        'DIRECTBANK' :: character varying,
                        'PAYMENTPLAN' :: character varying,
                        'ACCOUNTCREDIT' :: character varying]
                    ):: text[]
                )
            ),
    shipping_module_code    varchar(255),
    cart_code               varchar(255),
    order_status            varchar(255)
        constraint orders_order_status_check check (
            (order_status):: text = ANY (
                (
                    ARRAY [ 'ORDERED' :: character varying,
                        'PROCESSED' :: character varying,
                        'DELIVERED' :: character varying,
                        'REFUNDED' :: character varying, 'CANCELED' :: character varying]
                    ):: text[]
                )
            ),
    order_total             numeric(38, 2),
    billing_country_id      integer     not null
        constraint fk5ynmp6ibx15b6xilw3d8ssqt6 references store.country,
    billing_zone_id         bigint
        constraint fkhuynwr7s98867e5ooemlmcagu references store.zone,
    currency_id             bigint
        constraint fk4g9gno7ww2i06rxkfl3vbrgcu references store.currency,
    delivery_country_id     integer
        constraint fk4fi5e3yv4cl23arphenx4cc4a references store.country,
    delivery_zone_id        bigint
        constraint fkehld6io3ukp9wm0fypp04dbfg references store.zone,
    merchantid              integer
        constraint fkgdi0met0f4kt5mvhkt1hh51un references store.merchant_store
);
create table if not exists store.order_account
(
    order_account_id         bigint  not null primary key,
    order_account_bill_day   integer not null,
    order_account_end_date   date,
    order_account_start_date date    not null,
    order_id                 bigint  not null
        constraint fktdb599f1si18ktq25o4w5tsau references store.orders
);
create table if not exists store.order_attribute
(
    order_attribute_id bigint       not null primary key,
    identifier         varchar(255) not null,
    value              varchar(255) not null,
    order_id           bigint       not null
        constraint fkpfbrs3waqlbp0yeohck8sx91c references store.orders
);
create table if not exists store.order_product
(
    order_product_id bigint         not null primary key,
    onetime_charge   numeric(38, 2) not null,
    product_name     varchar(64)    not null,
    product_quantity integer,
    product_sku      varchar(255),
    order_id         bigint         not null
        constraint fkl5mnj9n0di7k1v90yxnthkc73 references store.orders
);
create table if not exists store.order_account_product
(
    order_account_product_id       bigint  not null primary key,
    order_account_product_accnt_dt date,
    order_account_product_end_dt   date,
    order_account_product_eot      timestamp(6),
    order_account_product_l_st_dt  timestamp(6),
    order_account_product_l_trx_st integer not null,
    order_account_product_pm_fr_ty integer not null,
    order_account_product_st_dt    date    not null,
    order_account_product_status   integer not null,
    order_account_id               bigint  not null
        constraint fk238g8uilgxlh5fa6ieub3ods references store.order_account,
    order_product_id               bigint  not null
        constraint fkl3rvhrb6nq9sdb1nai9cr9klm references store.order_product
);
create table if not exists store.order_product_attribute
(
    order_product_attribute_id bigint         not null primary key,
    product_attribute_is_free  boolean        not null,
    product_attribute_name     varchar(255),
    product_attribute_price    numeric(15, 4) not null,
    product_attribute_val_name varchar(255),
    product_attribute_weight   numeric(15, 4),
    product_option_id          bigint         not null,
    product_option_value_id    bigint         not null,
    order_product_id           bigint         not null
        constraint fk9w04pur2suf544spmowxfr3xg references store.order_product
);
create table if not exists store.order_product_download
(
    order_product_download_id bigint       not null primary key,
    download_count            integer      not null,
    download_maxdays          integer      not null,
    order_product_filename    varchar(255) not null,
    order_product_id          bigint       not null
        constraint fkmy1bxlfoja5v2pmo9vq76l7ry references store.order_product
);
create table if not exists store.order_product_price
(
    order_product_price_id   bigint         not null primary key,
    default_price            boolean        not null,
    product_price            numeric(38, 2) not null,
    product_price_code       varchar(64)    not null,
    product_price_name       varchar(255),
    product_price_special    numeric(38, 2),
    prd_price_special_end_dt timestamp(6),
    prd_price_special_st_dt  timestamp(6),
    order_product_id         bigint         not null
        constraint fkoh8f95nugkcqxflqo1rist0g1 references store.order_product
);
create table if not exists store.order_status_history
(
    order_status_history_id bigint       not null primary key,
    comments                text,
    customer_notified       integer,
    date_added              timestamp(6) not null,
    status                  varchar(255)
        constraint order_status_history_status_check check (
            (status):: text = ANY (
                (
                    ARRAY [ 'ORDERED' :: character varying,
                        'PROCESSED' :: character varying,
                        'DELIVERED' :: character varying,
                        'REFUNDED' :: character varying, 'CANCELED' :: character varying]
                    ):: text[]
                )
            ),
    order_id                bigint       not null
        constraint fknmcbg3mmbt8wfva97ra40nmp3 references store.orders
);
create table if not exists store.order_total
(
    order_account_id bigint         not null primary key,
    module           varchar(60),
    code             varchar(255)   not null,
    order_total_type varchar(255)
        constraint order_total_order_total_type_check check (
            (order_total_type):: text = ANY (
                (
                    ARRAY [ 'SHIPPING' :: character varying,
                        'HANDLING' :: character varying, 'TAX' :: character varying,
                        'PRODUCT' :: character varying, 'SUBTOTAL' :: character varying,
                        'TOTAL' :: character varying, 'CREDIT' :: character varying,
                        'REFUND' :: character varying]
                    ):: text[]
                )
            ),
    order_value_type varchar(255)
        constraint order_total_order_value_type_check check (
            (order_value_type):: text = ANY (
                (
                    ARRAY [ 'ONE_TIME' :: character varying,
                        'MONTHLY' :: character varying]
                    ):: text[]
                )
            ),
    sort_order       integer        not null,
    text             text,
    title            varchar(255),
    value            numeric(15, 4) not null,
    order_id         bigint         not null
        constraint fksyu55314fmsbvx76nxyvo2ejj references store.orders
);
create table if not exists store.product_option
(
    product_option_id       bigint  not null primary key,
    product_option_code     varchar(255),
    product_option_sort_ord integer,
    product_option_type     varchar(10),
    product_option_read     boolean,
    merchant_id             integer not null
        constraint fk170kledavgj3195k0thrne5ap references store.merchant_store,
    constraint ukcueag9xcotjhcm8lw6cwg4ciw unique (
                                                   merchant_id, product_option_code
        )
);
create index if not exists prd_option_code_idx on store.product_option (product_option_code);
create table if not exists store.product_option_desc
(
    description_id         bigint       not null primary key,
    date_created           timestamp(6),
    date_modified          timestamp(6),
    updt_id                varchar(60),
    description            text,
    name                   varchar(120) not null,
    title                  varchar(100),
    product_option_comment varchar(4000),
    language_id            integer      not null
        constraint fkp3ifcq30la3xh2hor0s4pb6j6 references store.language,
    product_option_id      bigint       not null
        constraint fktrmohor3afrj5vhs5rawi8vu0 references store.product_option,
    constraint ukq2vbyw6wuwqonud6pa6sixn57 unique (product_option_id, language_id)
);
create table if not exists store.product_option_set
(
    product_option_set_id   bigint  not null primary key,
    product_option_set_code varchar(255),
    product_option_set_disp boolean,
    product_option_id       bigint  not null
        constraint fka602atlw6q67lrmw7tp5i071q references store.product_option,
    merchant_id             integer not null
        constraint fk8vc1wowvwfenq3r6evlvym33u references store.merchant_store,
    constraint uko6klom5oqsfdd10kwtlb36vu0 unique (
                                                   merchant_id, product_option_set_code
        )
);
create table if not exists store.product_option_value
(
    product_option_value_id  bigint  not null primary key,
    product_option_val_code  varchar(255),
    product_opt_for_disp     boolean,
    product_opt_val_image    varchar(255),
    product_opt_val_sort_ord integer,
    merchant_id              integer not null
        constraint fkatyp79676dqksps89ustg7rxl references store.merchant_store,
    constraint uk5gus3qerf7649mgredge03qgl unique (
                                                   merchant_id, product_option_val_code
        )
);
create table if not exists store.product_opt_set_opt_value
(
    product_option_set_product_option_set_id bigint not null
        constraint fkdueqvdjymkdkcjynggnmmahv9 references store.product_option_set,
    values_product_option_value_id           bigint not null
        constraint fk2fdwprg8xh06io1alw7v4lkq6 references store.product_option_value
);
create index if not exists prd_option_val_code_idx on store.product_option_value (product_option_val_code);
create table if not exists store.product_option_value_description
(
    description_id          bigint       not null primary key,
    date_created            timestamp(6),
    date_modified           timestamp(6),
    updt_id                 varchar(60),
    description             text,
    name                    varchar(120) not null,
    title                   varchar(100),
    language_id             integer      not null
        constraint fkkrhgf4hps8ynjj4wcof292qtt references store.language,
    product_option_value_id bigint
        constraint fknl1ctkjjk7dn2g94n3w2lf6ro references store.product_option_value,
    constraint uk1vmuygxxf688xmi4s4jfid6sf unique (
                                                   product_option_value_id, language_id
        )
);
create table if not exists store.product_type
(
    product_type_id      bigint not null primary key,
    prd_type_add_to_cart boolean,
    date_created         timestamp(6),
    date_modified        timestamp(6),
    updt_id              varchar(60),
    prd_type_code        varchar(255),
    prd_type_visible     boolean,
    merchant_id          integer
        constraint fkngfrya457x3tdwc3rr90jcr69 references store.merchant_store
);
create table if not exists store.product_opt_set_prd_type
(
    product_option_set_product_option_set_id bigint not null
        constraint fkcglxrfpokkw8ovxjlhcewbgpp references store.product_option_set,
    product_types_product_type_id            bigint not null
        constraint fkfik3p80lpd24it8slpqlcfe3q references store.product_type,
    primary key (
                 product_option_set_product_option_set_id,
                 product_types_product_type_id
        )
);
create table if not exists store.product_type_description
(
    description_id  bigint       not null primary key,
    date_created    timestamp(6),
    date_modified   timestamp(6),
    updt_id         varchar(60),
    description     text,
    name            varchar(120) not null,
    title           varchar(100),
    language_id     integer      not null
        constraint fk1kksm1bur0ue7684apw7k5m9s references store.language,
    product_type_id bigint       not null
        constraint fk5yingh0egjkus0xfkl1hhmwy references store.product_type,
    constraint uk8xd9w50ra7erihiv86qvxhh4b unique (product_type_id, language_id)
);
create table if not exists store.product_variant_group
(
    product_variant_group_id bigint  not null primary key,
    merchant_id              integer not null
        constraint fkfnk2y3hfblgftaduiey0exnro references store.merchant_store
);
create table if not exists store.product_var_image
(
    product_var_image_id     bigint not null primary key,
    default_image            boolean,
    product_image            varchar(255),
    product_variant_group_id bigint not null
        constraint fkdw60r3ujcjs86ljprqecoueu references store.product_variant_group
);
create table if not exists store.product_variation
(
    product_variation_id bigint       not null primary key,
    date_created         timestamp(6),
    date_modified        timestamp(6),
    updt_id              varchar(60),
    code                 varchar(100) not null,
    sort_order           integer,
    variant_default      boolean,
    merchant_id          integer      not null
        constraint fkin0jfrl3v9hmw7mneib30je0n references store.merchant_store,
    product_option_id    bigint       not null
        constraint fkqsuew0y7i60q5x056w7qymhtq references store.product_option,
    option_value_id      bigint       not null
        constraint fkmdirohj7ym0mj2l1dl21yr8ur references store.product_option_value,
    constraint uk9ycrch79bt6ey52atmwve4x0q unique (
                                                   merchant_id, product_option_id, option_value_id
        )
);
create table if not exists store.shiping_origin
(
    ship_origin_id bigint  not null primary key,
    active         boolean,
    street_address varchar(256),
    city           varchar(100),
    postcode       varchar(20),
    state          varchar(100),
    country_id     integer
        constraint fk1sodi5w3xrkk8roguytwpg413 references store.country,
    merchant_id    integer not null
        constraint fkahnd2m0h56gj13kqcc7u9su18 references store.merchant_store,
    zone_id        bigint
        constraint fkqlt6eky3radsqa21sh4ku4bxy references store.zone
);
create table if not exists store.shipping_quote
(
    shipping_quote_id       bigint       not null primary key,
    cart_id                 bigint,
    customer_id             bigint,
    delivery_street_address varchar(256),
    delivery_city           varchar(100),
    delivery_company        varchar(100),
    delivery_first_name     varchar(64),
    delivery_last_name      varchar(64),
    delivery_postcode       varchar(20),
    delivery_state          varchar(100),
    delivery_telephone      varchar(32),
    shipping_number_days    integer,
    free_shipping           boolean,
    quote_handling          numeric(38, 2),
    ip_address              varchar(255),
    module                  varchar(255) not null,
    option_code             varchar(255),
    option_delivery_date    timestamp(6),
    option_name             varchar(255),
    option_shipping_date    timestamp(6),
    order_id                bigint,
    quote_price             numeric(38, 2),
    quote_date              timestamp(6),
    delivery_country_id     integer
        constraint fklqymoy371jgpl2l3qmynr54gh references store.country,
    delivery_zone_id        bigint
        constraint fk3beir3ypxv4iw1y0ybvxu6bwu references store.zone
);
create table if not exists store.shopping_cart
(
    shp_cart_id   bigint       not null primary key,
    date_created  timestamp(6),
    date_modified timestamp(6),
    updt_id       varchar(60),
    customer_id   bigint,
    ip_address    varchar(255),
    order_id      bigint,
    promo_added   timestamp(6),
    promo_code    varchar(255),
    shp_cart_code varchar(255) not null
        constraint uk_g6b5qebd5yvy3msjrus23vw51 unique,
    merchant_id   integer      not null
        constraint fk34a665fn3dmkxhgs968u2aa6s references store.merchant_store
);
create index if not exists shp_cart_code_idx on store.shopping_cart (shp_cart_code);
create index if not exists shp_cart_customer_idx on store.shopping_cart (customer_id);
create table if not exists store.shopping_cart_item
(
    shp_cart_item_id bigint not null primary key,
    date_created     timestamp(6),
    date_modified    timestamp(6),
    updt_id          varchar(60),
    product_id       bigint not null,
    quantity         integer,
    sku              varchar(255),
    product_variant  bigint,
    shp_cart_id      bigint not null
        constraint fk10kmhpldycqc7cvn24tesj8yx references store.shopping_cart
);
create table if not exists store.shopping_cart_attr_item
(
    shp_cart_attr_item_id bigint not null primary key,
    date_created          timestamp(6),
    date_modified         timestamp(6),
    updt_id               varchar(60),
    product_attr_id       bigint not null,
    shp_cart_item_id      bigint not null
        constraint fkt3iw5nxx7h55j5vta1tyrvgv3 references store.shopping_cart_item
);
create table if not exists store.sm_transaction
(
    transaction_id   bigint not null primary key,
    amount           numeric(38, 2),
    date_created     timestamp(6),
    date_modified    timestamp(6),
    updt_id          varchar(60),
    details          text,
    payment_type     varchar(255)
        constraint sm_transaction_payment_type_check check (
            (payment_type):: text = ANY (
                (
                    ARRAY [ 'CREDITCARD' :: character varying,
                        'FREE' :: character varying, 'COD' :: character varying,
                        'MONEYORDER' :: character varying,
                        'PAYPAL' :: character varying, 'INVOICE' :: character varying,
                        'DIRECTBANK' :: character varying,
                        'PAYMENTPLAN' :: character varying,
                        'ACCOUNTCREDIT' :: character varying]
                    ):: text[]
                )
            ),
    transaction_date timestamp(6),
    transaction_type varchar(255)
        constraint sm_transaction_transaction_type_check check (
            (transaction_type):: text = ANY (
                (
                    ARRAY [ 'INIT' :: character varying,
                        'AUTHORIZE' :: character varying,
                        'CAPTURE' :: character varying, 'AUTHORIZECAPTURE' :: character varying,
                        'REFUND' :: character varying, 'OK' :: character varying]
                    ):: text[]
                )
            ),
    order_id         bigint
        constraint fkdgyct8065xy9kp7entj7lcgsj references store.orders
);
create table if not exists store.tax_class
(
    tax_class_id    bigint      not null primary key,
    tax_class_code  varchar(10) not null,
    tax_class_title varchar(32) not null,
    merchant_id     integer
        constraint fk7oovh2wiim8s1nuppyxl8dcms references store.merchant_store,
    constraint uk4bosvt4pv3waxai9yfa76nn0a unique (merchant_id, tax_class_code)
);
create table if not exists store.product
(
    product_id       bigint  not null primary key,
    date_created     timestamp(6),
    date_modified    timestamp(6),
    updt_id          varchar(60),
    available        boolean,
    cond             smallint
        constraint product_cond_check check (
            (cond >= 0)
                AND (cond <= 1)
            ),
    date_available   timestamp(6),
    preorder         boolean,
    product_height   numeric(38, 2),
    product_free     boolean,
    product_length   numeric(38, 2),
    quantity_ordered integer,
    review_avg       numeric(38, 2),
    review_count     integer,
    product_ship     boolean,
    product_virtual  boolean,
    product_weight   numeric(38, 2),
    product_width    numeric(38, 2),
    ref_sku          varchar(255),
    rental_duration  integer,
    rental_period    integer,
    rental_status    smallint
        constraint product_rental_status_check check (
            (rental_status >= 0)
                AND (rental_status <= 1)
            ),
    sku              varchar(255),
    sort_order       integer,
    manufacturer_id  bigint
        constraint fk89igr5j06uw5ps04djxgom0l1 references store.manufacturer,
    merchant_id      integer not null
        constraint fkd6g3hw493iyhotyyo2k31te6n references store.merchant_store,
    customer_id      bigint
        constraint fkj80n6400wnfqrt86qimf9k6ys references store.customer,
    tax_class_id     bigint
        constraint fklvugcfbptp5ey8cp4x65ck4p references store.tax_class,
    product_type_id  bigint
        constraint fklabq3c2e90ybbxk58rc48byqo references store.product_type,
    constraint ukmsy2l9x6ji651n5yrpflbqc3c unique (merchant_id, sku)
);
create table if not exists store.product_attribute
(
    product_attribute_id         bigint not null primary key,
    product_attribute_default    boolean,
    product_attribute_discounted boolean,
    product_attribute_for_disp   boolean,
    product_attribute_required   boolean,
    product_attribute_free       boolean,
    product_atribute_price       numeric(38, 2),
    product_attribute_weight     numeric(38, 2),
    product_attribute_sort_ord   integer,
    product_id                   bigint not null
        constraint fklefs59y5kmsbu017n1wp10gf2 references store.product,
    option_id                    bigint not null
        constraint fk9bv3sx347ljlhjp2vtghkd9om references store.product_option,
    option_value_id              bigint not null
        constraint fk136df306o3xt00l44t3708t23 references store.product_option_value,
    constraint ukd10bjk5oofj1a7e3xbct88mj9 unique (
                                                   option_id, option_value_id, product_id
        )
);
create index if not exists idxdnt1duep1jr86pow6xsujmx84 on store.product_attribute (product_id);
create table if not exists store.product_category
(
    product_id  bigint not null
        constraint fk2k3smhbruedlcrvu6clued06x references store.product,
    category_id bigint not null
        constraint fkkud35ls1d40wpjb5htpp14q4e references store.category,
    primary key (product_id, category_id)
);
create table if not exists store.product_description
(
    description_id    bigint       not null primary key,
    date_created      timestamp(6),
    date_modified     timestamp(6),
    updt_id           varchar(60),
    description       text,
    name              varchar(120) not null,
    title             varchar(100),
    meta_description  varchar(255),
    meta_keywords     varchar(255),
    meta_title        varchar(255),
    download_lnk      varchar(255),
    product_highlight varchar(255),
    sef_url           varchar(255),
    language_id       integer      not null
        constraint fkku17r6yhalvxil8504iqr59ju references store.language,
    product_id        bigint       not null
        constraint fk9iiotbwtk1n1b6dgga729sg9q references store.product,
    constraint ukn8onply0evnma0i5p5c9gxy0s unique (product_id, language_id)
);
create index if not exists product_description_sef_url on store.product_description (sef_url);
create table if not exists store.product_digital
(
    product_digital_id bigint       not null primary key,
    file_name          varchar(255) not null,
    product_id         bigint       not null
        constraint fkr5um4hejfu56oysveb3e5xs8j references store.product,
    constraint ukg6qeecrifgaebprc185e880lq unique (product_id, file_name)
);
create table if not exists store.product_image
(
    product_image_id  bigint not null primary key,
    default_image     boolean,
    image_crop        boolean,
    image_type        integer,
    product_image     varchar(255),
    product_image_url varchar(255),
    sort_order        integer,
    product_id        bigint not null
        constraint fk6oo0cvcdtb6qmwsga468uuukk references store.product
);
create table if not exists store.product_image_description
(
    description_id   bigint       not null primary key,
    date_created     timestamp(6),
    date_modified    timestamp(6),
    updt_id          varchar(60),
    description      text,
    name             varchar(120) not null,
    title            varchar(100),
    alt_tag          varchar(100),
    language_id      integer      not null
        constraint fkqr8g6kxnfke72qcwxdmwpqe6j references store.language,
    product_image_id bigint       not null
        constraint fk9k3u9pf3teymlxchgu9p4jd9e references store.product_image,
    constraint uk6y94rqwurfscf7w7y42gnakfa unique (product_image_id, language_id)
);
create table if not exists store.product_relationship
(
    product_relationship_id bigint  not null primary key,
    active                  boolean,
    code                    varchar(255),
    product_id              bigint
        constraint fk7opqd0ko4gr45r2lpmiyr9sai references store.product,
    related_product_id      bigint
        constraint fkg6fbs35996omj1d4iw2upkdmn references store.product,
    merchant_id             integer not null
        constraint fkegrxb6o6wlsw2i1fnh34ifwpg references store.merchant_store
);
create table if not exists store.product_review
(
    product_review_id bigint not null primary key,
    date_created      timestamp(6),
    date_modified     timestamp(6),
    updt_id           varchar(60),
    review_date       timestamp(6),
    reviews_rating    double precision,
    reviews_read      bigint,
    status            integer,
    customers_id      bigint
        constraint fkofeixop0udspl2l0l617um4j4 references store.customer,
    product_id        bigint
        constraint uk_59pjh8ssspqs6yypt6kqxtb8r unique
        constraint fkkaqmhakwt05p3n0px81b9pdya references store.product,
    constraint ukceks6ffaawgis0aa58s7fpxfc unique (customers_id, product_id)
);
create table if not exists store.product_review_description
(
    description_id    bigint       not null primary key,
    date_created      timestamp(6),
    date_modified     timestamp(6),
    updt_id           varchar(60),
    description       text,
    name              varchar(120) not null,
    title             varchar(100),
    language_id       integer      not null
        constraint fk3wrvfyaqeji5ax62poagpadst references store.language,
    product_review_id bigint
        constraint fkcv4tyhkx5sid0tqeofg3f4oa0 references store.product_review,
    constraint uk12abuw7g7vx45bgvq1ygydxbu unique (product_review_id, language_id)
);
create table if not exists store.product_var_image_description
(
    description_id       bigint       not null primary key,
    date_created         timestamp(6),
    date_modified        timestamp(6),
    updt_id              varchar(60),
    description          text,
    name                 varchar(120) not null,
    title                varchar(100),
    alt_tag              varchar(100),
    language_id          integer      not null
        constraint fk3fis5bi7ket58o8udwigjh82h references store.language,
    product_id           bigint       not null
        constraint fkhn2mq2b7cqn7rg7h4k9py71k6 references store.product,
    product_var_image_id bigint       not null
        constraint fkdafb5lwa8xuw5knggb5opunqw references store.product_var_image,
    constraint uks4cdvf1bcgnse9p0vpukj2mk9 unique (
                                                   product_var_image_id, language_id
        )
);
create table if not exists store.product_variant
(
    product_variant_id         bigint not null primary key,
    date_created               timestamp(6),
    date_modified              timestamp(6),
    updt_id                    varchar(60),
    available                  boolean,
    code                       varchar(255),
    date_available             timestamp(6),
    default_selection          boolean,
    sku                        varchar(255),
    sort_order                 integer,
    product_id                 bigint not null
        constraint fkgrbbs9t374m9gg43l6tq1xwdj references store.product,
    product_variant_group_id   bigint
        constraint fk8xtj3bi1s75coa9f84tgof611 references store.product_variant_group,
    product_variation_id       bigint
        constraint fkrsayulwa4xtlt8mc3sddl1spt references store.product_variation,
    product_variation_value_id bigint
        constraint fkrt8wxodoxdqx5t4a1ey66xnyd references store.product_variation,
    constraint ukg69sutihl26shxl3w47s0vch unique (product_id, sku)
);
create table if not exists store.product_availability
(
    product_avail_id bigint  not null primary key,
    date_created     timestamp(6),
    date_modified    timestamp(6),
    updt_id          varchar(60),
    available        boolean,
    height           numeric(38, 2),
    length           numeric(38, 2),
    weight           numeric(38, 2),
    width            numeric(38, 2),
    owner            varchar(255),
    date_available   date,
    free_shipping    boolean,
    quantity         integer not null,
    quantity_ord_max integer,
    quantity_ord_min integer,
    status           boolean,
    region           varchar(255),
    region_variant   varchar(255),
    sku              varchar(255),
    merchant_id      integer
        constraint fkjd7yvpi4dongh1kb4sqwro0m1 references store.merchant_store,
    product_id       bigint  not null
        constraint fk3sgpu0mqt3cncaw0k1x3okkq8 references store.product,
    product_variant  bigint
        constraint fkg460g5177h3t14atnsynm8ikc references store.product_variant,
    constraint ukbmvos7ykpq71uft8gsdmuq0fn unique (
                                                   merchant_id, product_id, product_variant,
                                                   region_variant
        )
);
create index if not exists prd_avail_store_prd_idx on store.product_availability (product_id, merchant_id);
create index if not exists prd_avail_prd_idx on store.product_availability (product_id);
create table if not exists store.product_price
(
    product_price_id               bigint       not null primary key,
    product_price_code             varchar(255) not null,
    default_price                  boolean,
    product_identifier_id          bigint,
    product_price_amount           numeric(38, 2),
    product_price_special_amount   numeric(38, 2),
    product_price_special_end_date date,
    product_price_special_st_date  date,
    product_price_type             varchar(20)
        constraint product_price_product_price_type_check check (
            (product_price_type):: text = ANY (
                (
                    ARRAY [ 'ONE_TIME' :: character varying,
                        'MONTHLY' :: character varying]
                    ):: text[]
                )
            ),
    product_avail_id               bigint       not null
        constraint fkth2oyk1000w7vut9aro9a8mhi references store.product_availability
);
create table if not exists store.product_price_description
(
    description_id   bigint       not null primary key,
    date_created     timestamp(6),
    date_modified    timestamp(6),
    updt_id          varchar(60),
    description      text,
    name             varchar(120) not null,
    title            varchar(100),
    price_appender   varchar(255),
    language_id      integer      not null
        constraint fk2vyx638admfe7pfepeicyblrs references store.language,
    product_price_id bigint       not null
        constraint fkghwl7kccj71r4u0qdwegbles6 references store.product_price,
    constraint ukcxb8qgr4bg8a36cod9wkkailj unique (product_price_id, language_id)
);
create index if not exists idxv61l549h1wdlqi4chir5muwl on store.product_variant (product_id);
create index if not exists tax_class_code_idx on store.tax_class (tax_class_code);
create table if not exists store.tax_rate
(
    tax_rate_id      bigint        not null primary key,
    date_created     timestamp(6),
    date_modified    timestamp(6),
    updt_id          varchar(60),
    tax_code         varchar(255),
    piggyback        boolean,
    store_state_prov varchar(100),
    tax_priority     integer,
    tax_rate         numeric(7, 4) not null,
    country_id       integer       not null
        constraint fk6nx17w066c6xyoixlox1w1ljr references store.country,
    merchant_id      integer       not null
        constraint fkdfopp56727rdxxsikrokhlc5g references store.merchant_store,
    parent_id        bigint
        constraint fkhm0j4gnvkomofkt9icu5a2rwj references store.tax_rate,
    tax_class_id     bigint        not null
        constraint fkdpsh7ijtsvl76sdf3owmw85ti references store.tax_class,
    zone_id          bigint
        constraint uk_mg3dtroau6627erjklnh1xco9 unique
        constraint fkslh4l8myhwh28bh4otco52b7d references store.zone,
    constraint ukjeva529krcnmobeqftq4e26pc unique (tax_code, merchant_id)
);
create table if not exists store.tax_rate_description
(
    description_id bigint       not null primary key,
    date_created   timestamp(6),
    date_modified  timestamp(6),
    updt_id        varchar(60),
    description    text,
    name           varchar(120) not null,
    title          varchar(100),
    language_id    integer      not null
        constraint fkm6rc5u361xdpyxnp18sn0p2bh references store.language,
    tax_rate_id    bigint
        constraint fkcy14oyw2f30xg3mdswvcsy66 references store.tax_rate,
    constraint ukgi5fqf4m874igqntxqy2ja5a unique (tax_rate_id, language_id)
);
create table if not exists store.users
(
    user_id               bigint  not null primary key,
    active                boolean,
    admin_email           varchar(255),
    admin_name            varchar(100),
    admin_password        varchar(80),
    admin_a1              varchar(255),
    admin_a2              varchar(255),
    admin_a3              varchar(255),
    date_created          timestamp(6),
    date_modified         timestamp(6),
    updt_id               varchar(60),
    reset_credentials_req varchar(256),
    reset_credentials_exp date,
    admin_first_name      varchar(255),
    last_access           timestamp(6),
    admin_last_name       varchar(255),
    login_access          timestamp(6),
    admin_q1              varchar(255),
    admin_q2              varchar(255),
    admin_q3              varchar(255),
    language_id           integer
        constraint fkld9x6vyo1xd6shjgkojm0fqmt references store.language,
    merchant_id           integer not null
        constraint fkd8nvdkilwmegy0ub7qwb28h74 references store.merchant_store,
    constraint ukku21as0pwmmc6bvkffe33ufvm unique (merchant_id, admin_name)
);
create table if not exists store.user_group
(
    user_id  bigint  not null
        constraint fk7k9ade3lqbo483u9vuryxmm34 references store.users,
    group_id integer not null
        constraint fk1affuy6buav1nuigp9musflp9 references store.sm_group
);
create index if not exists usr_name_idx on store.users (admin_name);
create table if not exists store.zone_description
(
    description_id bigint       not null primary key,
    date_created   timestamp(6),
    date_modified  timestamp(6),
    updt_id        varchar(60),
    description    text,
    name           varchar(120) not null,
    title          varchar(100),
    language_id    integer      not null
        constraint fk3bhgm7w9u4lmb1oct3icvp55j references store.language,
    zone_id        bigint       not null
        constraint fkkgubo9lvv7fyi3yug7692evh9 references store.zone,
    constraint uk1lywe2n7c0qe9mvn1mjogivmi unique (zone_id, language_id)
);
