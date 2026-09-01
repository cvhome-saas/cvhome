import {getTranslations} from 'next-intl/server';
import {FacebookIcon, GithubIcon, InstagramIcon, LinkedinIcon, TwitterIcon, YoutubeIcon, type LucideIcon} from 'lucide-react';
import {Link} from '@store-front/i18n/navigation';
import type {LayoutData, PageContext} from '@store-front/theme';

const SOCIAL_ICONS: Record<string, LucideIcon> = {
    facebook: FacebookIcon, twitter: TwitterIcon, x: TwitterIcon, instagram: InstagramIcon, github: GithubIcon,
    youtube: YoutubeIcon, linkedin: LinkedinIcon,
};

/** The colophon: the issue closes on an ink page, the imprint set large, everything else ruled in columns. */
export async function Footer({data}: { ctx: PageContext; data: LayoutData }) {
    const t = await getTranslations('COMPONENTS.FOOTER');
    const {store} = data;
    const shop = data.categories.filter(c => c.description).slice(0, 8);
    const info = data.pages;
    // Footer-menu entries that are not pages, then the live legal policies — all resolved server-side.
    const more = [
        ...data.menus.footer.filter(n => n.kind !== 'PAGE' && n.href.startsWith('/') && !n.href.startsWith('/policies/')).map(n => ({key: n.href, href: n.href, label: n.label})),
        ...data.policies.map(pl => ({key: pl.href, href: pl.href, label: pl.title})),
    ];
    const socials = data.socialLinks.filter(s => SOCIAL_ICONS[s.provider.toLowerCase()]);
    const column = 'flex flex-col gap-2.5';
    const heading = 'cover-line pb-2 opacity-70';
    const linkCls = 'text-sm font-medium hover:underline';
    return (
        <footer className="ink-field mt-section">
            <div className="mx-auto max-w-content px-gutter py-10 lg:py-14">
                <p className="display text-4xl sm:text-5xl lg:text-6xl">{store.name}</p>
                <div className="mt-8 grid gap-8 border-t border-current/25 pt-8 sm:grid-cols-2 lg:grid-cols-4">
                    <div className={column}>
                        <h2 className={heading}>{t('CONTACT')}</h2>
                        {(store.address?.address || store.address?.city) && (
                            <address className="text-sm not-italic opacity-80">
                                {[store.address.address, store.address.city, store.address.postalCode, store.address.country].filter(Boolean).join(', ')}
                            </address>
                        )}
                        {store.email && <a href={`mailto:${store.email}`} className={linkCls}>{store.email}</a>}
                        {store.phone && <a href={`tel:${store.phone}`} className={`${linkCls} figure`} dir="ltr">{store.phone}</a>}
                        {socials.length > 0 && (
                            <ul className="mt-2 flex gap-2" aria-label={t('FOLLOW_US')}>
                                {socials.map(s => {
                                    const Icon = SOCIAL_ICONS[s.provider.toLowerCase()];
                                    const url = s.url.startsWith('http') ? s.url : `https://${s.url}`;
                                    return (
                                        <li key={s.provider}>
                                            <a href={url} target="_blank" rel="noopener noreferrer" aria-label={s.provider}
                                               className="flex size-9 items-center justify-center border border-current/40 transition-colors duration-(--motion-fast) hover:bg-primary hover:text-primary-foreground">
                                                <Icon className="size-4"/>
                                            </a>
                                        </li>
                                    );
                                })}
                            </ul>
                        )}
                    </div>
                    {shop.length > 0 && (
                        <nav aria-label={t('SHOP')} className={column}>
                            <h2 className={heading}>{t('SHOP')}</h2>
                            {shop.map((c, i) => (
                                <Link key={c.code} prefetch={false} href={`/category/${c.description.friendlyUrl}`} className={`${linkCls} flex gap-3`}>
                                    <span aria-hidden className="figure opacity-60">{String(i + 1).padStart(2, '0')}</span>{c.description.name}
                                </Link>
                            ))}
                        </nav>
                    )}
                    {(info.length > 0 || more.length > 0) && (
                        <nav aria-label={t('INFORMATION')} className={column}>
                            <h2 className={heading}>{t('INFORMATION')}</h2>
                            {info.map(p => <Link key={p.code} prefetch={false} href={p.href} className={linkCls}>{p.name}</Link>)}
                            {more.map(l => <Link key={l.key} prefetch={false} href={l.href} className={linkCls}>{l.label}</Link>)}
                        </nav>
                    )}
                </div>
            </div>
            <div className="border-t border-current/25">
                <p className="mx-auto max-w-content px-gutter py-4 text-xs opacity-75">
                    © {new Date().getFullYear()} {store.name}. {t('RIGHT_RESERVED')}
                </p>
            </div>
        </footer>
    );
}
