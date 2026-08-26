import {getTranslations} from 'next-intl/server';
import {FacebookIcon, GithubIcon, InstagramIcon, LinkedinIcon, TwitterIcon, YoutubeIcon, type LucideIcon} from 'lucide-react';
import {Link} from '@store-front/i18n/navigation';
import type {LayoutData, PageContext} from '@store-front/theme';

const SOCIAL_ICONS: Record<string, LucideIcon> = {
    facebook: FacebookIcon, twitter: TwitterIcon, x: TwitterIcon, instagram: InstagramIcon, github: GithubIcon,
    youtube: YoutubeIcon, linkedin: LinkedinIcon,
};

/**
 * The colophon at the foot of the building: the directory repeated small with its floor numbers intact,
 * the information desk, and the address plate. Set on a terrazzo landing so the page closes on ground
 * rather than trailing off.
 *
 * The address is wrapped in `<bdi>`: a merchant address usually starts with a house number, and in an
 * Arabic paragraph that leading number is reordered to the end of the line unless the string is isolated.
 */
export async function Footer({data}: { ctx: PageContext; data: LayoutData }) {
    const t = await getTranslations('COMPONENTS.FOOTER');
    const tc = await getTranslations('COMMON');
    const {store} = data;
    const shop = data.categories.filter(c => c.description).slice(0, 8);
    const info = data.pages;
    // Footer-menu entries that are not pages, then the live legal policies — all resolved server-side.
    const more = [
        ...data.menus.footer.filter(n => n.kind !== 'PAGE' && n.href.startsWith('/')).map(n => ({key: n.href, href: n.href, label: n.label})),
        ...data.policies.map(pl => ({key: pl.href, href: pl.href, label: pl.title})),
    ];
    const socials = (store.socialLinks ?? []).filter(s => SOCIAL_ICONS[s.provider.toLowerCase()]);
    const heading = 'sign rule-brass mb-4 border-b pb-2 text-[0.625rem] text-muted-foreground';

    return (
        <footer className="terrazzo border-t border-[var(--brass)]">
            <div className="mx-auto grid max-w-content gap-10 px-gutter py-12 sm:grid-cols-2 lg:grid-cols-[1.2fr_1fr_1fr_1fr] lg:gap-12 lg:py-16">
                <div className="flex flex-col gap-4">
                    <p className="sign-lg text-xl">{store.name}</p>
                    {(store.address?.address || store.address?.city) && (
                        <address className="text-sm not-italic leading-relaxed text-muted-foreground">
                            <bdi>{[store.address.address, store.address.city, store.address.postalCode, store.address.country].filter(Boolean).join(', ')}</bdi>
                        </address>
                    )}
                    {socials.length > 0 && (
                        <ul className="mt-1 flex gap-2" aria-label={t('FOLLOW_US')}>
                            {socials.map(s => {
                                const Icon = SOCIAL_ICONS[s.provider.toLowerCase()];
                                const url = s.url.startsWith('http') ? s.url : `https://${s.url}`;
                                return (
                                    <li key={s.provider}>
                                        <a href={url} target="_blank" rel="noopener noreferrer" aria-label={s.provider}
                                           className="rule-brass flex size-9 items-center justify-center rounded-control border bg-background transition-colors duration-(--motion-fast) hover:bg-primary hover:text-primary-foreground">
                                            <Icon className="size-4"/>
                                        </a>
                                    </li>
                                );
                            })}
                        </ul>
                    )}
                </div>

                {shop.length > 0 && (
                    <nav aria-label={tc('DIRECTORY')} className="flex flex-col">
                        <h2 className={heading}>{tc('DIRECTORY')}</h2>
                        <ul className="flex flex-col gap-2.5">
                            {shop.map((c, i) => (
                                <li key={c.code}>
                                    <Link prefetch={false} href={`/category/${c.description.friendlyUrl}`}
                                          className="flex items-baseline gap-3 text-sm hover:underline">
                                        <span aria-hidden className="figure w-6 shrink-0 text-xs text-muted-foreground">{String(i + 1).padStart(2, '0')}</span>
                                        <span className="flex-1">{c.description.name}</span>
                                        {c.productCount > 0 && <span className="figure text-xs text-muted-foreground">{c.productCount}</span>}
                                    </Link>
                                </li>
                            ))}
                        </ul>
                    </nav>
                )}

                {(info.length > 0 || more.length > 0) && (
                    <nav aria-label={t('INFORMATION')} className="flex flex-col">
                        <h2 className={heading}>{t('INFORMATION')}</h2>
                        <ul className="flex flex-col gap-2.5">
                            {[...info.map(p => ({key: p.code, href: p.href, label: p.name})), ...more].map(l => (
                                <li key={l.key}><Link prefetch={false} href={l.href} className="text-sm hover:underline">{l.label}</Link></li>
                            ))}
                        </ul>
                    </nav>
                )}

                <div className="flex flex-col">
                    <h2 className={heading}>{t('CONTACT')}</h2>
                    <ul className="flex flex-col gap-2.5">
                        {store.email && <li><a href={`mailto:${store.email}`} className="text-sm hover:underline">{store.email}</a></li>}
                        {store.phone && <li><a href={`tel:${store.phone}`} className="figure text-sm hover:underline" dir="ltr">{store.phone}</a></li>}
                    </ul>
                </div>
            </div>
            <div className="border-t border-[var(--brass-faint)]">
                <p className="sign mx-auto max-w-content px-gutter py-5 text-[0.625rem] text-muted-foreground">
                    © {new Date().getFullYear()} {store.name}. {t('RIGHT_RESERVED')}
                </p>
            </div>
        </footer>
    );
}
