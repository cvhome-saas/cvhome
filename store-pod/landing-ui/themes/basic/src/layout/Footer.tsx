import {getTranslations} from 'next-intl/server';
import {FacebookIcon, GithubIcon, InstagramIcon, LinkedinIcon, TwitterIcon, YoutubeIcon, type LucideIcon} from 'lucide-react';
import {Link} from '@store-front/i18n/navigation';
import type {LayoutData, PageContext} from '@store-front/theme';

const SOCIAL_ICONS: Record<string, LucideIcon> = {
    facebook: FacebookIcon, twitter: TwitterIcon, x: TwitterIcon, instagram: InstagramIcon, github: GithubIcon,
    youtube: YoutubeIcon, linkedin: LinkedinIcon,
};

/** The back page: the store's facts, the index and the information pages in ruled cells, the imprint line under a rule. */
export async function Footer({data}: { ctx: PageContext; data: LayoutData }) {
    const t = await getTranslations('COMPONENTS.FOOTER');
    const th = await getTranslations('PAGE.HOME');
    const {store} = data;
    const shop = data.categories.filter(c => c.description && c.visible !== false).slice(0, 8);
    const info = data.pages.filter(p => p.description);
    const socials = (store.socialLinks ?? []).filter(s => SOCIAL_ICONS[s.provider.toLowerCase()]);
    const year = store.inBusinessSince ? new Date(store.inBusinessSince).getFullYear() : NaN;
    const address = [store.address?.address, store.address?.city, store.address?.postalCode, store.address?.country].filter(Boolean).join(', ');
    const head = 'mb-3 text-xs font-semibold uppercase tracking-wide text-muted-foreground';
    const link = 'text-sm hover:underline';
    return (
        <footer className="mt-section border-t">
            <div className="mx-auto max-w-content px-gutter py-10 lg:py-14">
                <div className="ruled sm:grid-cols-2 lg:grid-cols-4">
                    <div className="flex flex-col gap-3 p-5 lg:p-6">
                        <p className="display text-3xl"><bdi dir="auto">{store.name}</bdi></p>
                        {address && <address className="text-sm not-italic text-muted-foreground">{address}</address>}
                        {!Number.isNaN(year) && <p className="text-sm text-muted-foreground tabular-nums">{th('SINCE', {year})}</p>}
                    </div>
                    {shop.length > 0 && (
                        <nav aria-label={t('SHOP')} className="flex flex-col gap-1.5 p-5 lg:p-6">
                            <h2 className={head}>{t('SHOP')}</h2>
                            {shop.map(c => <Link key={c.code} prefetch={false} href={`/category/${c.description.friendlyUrl}`} className={link}><bdi dir="auto">{c.description.name}</bdi></Link>)}
                        </nav>
                    )}
                    {info.length > 0 && (
                        <nav aria-label={t('INFORMATION')} className="flex flex-col gap-1.5 p-5 lg:p-6">
                            <h2 className={head}>{t('INFORMATION')}</h2>
                            {info.map(p => <Link key={p.code} prefetch={false} href={`/content/${p.description.friendlyUrl}`} className={link}><bdi dir="auto">{p.description.name}</bdi></Link>)}
                        </nav>
                    )}
                    <div className="flex flex-col gap-1.5 p-5 lg:p-6">
                        <h2 className={head}>{t('CONTACT')}</h2>
                        {store.email && <a href={`mailto:${store.email}`} className={`${link} break-all`} dir="ltr">{store.email}</a>}
                        {store.phone && <a href={`tel:${store.phone}`} className={`${link} tabular-nums`} dir="ltr">{store.phone}</a>}
                        {socials.length > 0 && (
                            <ul className="mt-3 flex flex-wrap gap-2" aria-label={t('FOLLOW_US')}>
                                {socials.map(s => {
                                    const Icon = SOCIAL_ICONS[s.provider.toLowerCase()];
                                    const url = s.url.startsWith('http') ? s.url : `https://${s.url}`;
                                    return (
                                        <li key={s.provider}>
                                            <a href={url} target="_blank" rel="noopener noreferrer" aria-label={s.provider}
                                               className="flex size-10 items-center justify-center rounded-control border hover:bg-accent">
                                                <Icon className="size-4"/>
                                            </a>
                                        </li>
                                    );
                                })}
                            </ul>
                        )}
                    </div>
                </div>
                <p className="mt-5 flex flex-wrap items-center justify-between gap-x-6 gap-y-1 text-xs text-muted-foreground">
                    <span>© {new Date().getFullYear()} <bdi dir="auto">{store.name}</bdi>. {t('RIGHT_RESERVED')}</span>
                    {store.currency && <span className="tabular-nums" dir="ltr">{store.currency}</span>}
                </p>
            </div>
        </footer>
    );
}
