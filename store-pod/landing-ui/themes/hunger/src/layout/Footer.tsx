import {getTranslations} from 'next-intl/server';
import {FacebookIcon, GithubIcon, InstagramIcon, LinkedinIcon, TwitterIcon, YoutubeIcon, type LucideIcon} from 'lucide-react';
import {Link} from '@store-front/i18n/navigation';
import type {LayoutData, PageContext} from '@store-front/theme';

const SOCIAL_ICONS: Record<string, LucideIcon> = {
    facebook: FacebookIcon, twitter: TwitterIcon, x: TwitterIcon, instagram: InstagramIcon, github: GithubIcon,
    youtube: YoutubeIcon, linkedin: LinkedinIcon,
};

/**
 * The back of the sheet: how to reach the kitchen, printed large because that is what a takeaway menu is
 * for, then the sections and the small print in ruled columns.
 */
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
    const address = [store.address?.address, store.address?.city, store.address?.postalCode, store.address?.country].filter(Boolean).join(', ');

    return (
        <footer className="mt-section border-t-2 border-foreground">
            <div className="mx-auto max-w-content px-gutter py-10">
                <div className="flex flex-col gap-6 border-b border-border pb-8 lg:flex-row lg:items-end lg:justify-between">
                    <div className="flex flex-col gap-2">
                        <p className="press text-3xl leading-none sm:text-4xl">{store.name}</p>
                        {address && <address className="max-w-sm text-sm not-italic text-muted-foreground">{address}</address>}
                    </div>
                    {store.phone && (
                        <a href={`tel:${store.phone}`} className="press crop inline-flex items-baseline text-4xl leading-none text-primary hover:underline sm:text-5xl" dir="ltr">
                            {store.phone}
                        </a>
                    )}
                </div>

                <div className="grid gap-8 pt-8 sm:grid-cols-2 lg:grid-cols-3">
                    {shop.length > 0 && (
                        <nav aria-label={t('SHOP')} className="flex flex-col gap-2">
                            <h2 className="press border-b border-border pb-1 text-sm tracking-wide">{t('SHOP')}</h2>
                            {shop.map(c => (
                                <Link key={c.code} prefetch={false} href={`/category/${c.description.friendlyUrl}`} className="text-sm hover:underline">
                                    {c.description.name}
                                </Link>
                            ))}
                        </nav>
                    )}
                    {(info.length > 0 || more.length > 0) && (
                        <nav aria-label={t('INFORMATION')} className="flex flex-col gap-2">
                            <h2 className="press border-b border-border pb-1 text-sm tracking-wide">{t('INFORMATION')}</h2>
                            {info.map(p => <Link key={p.code} prefetch={false} href={p.href} className="text-sm hover:underline">{p.name}</Link>)}
                            {more.map(l => <Link key={l.key} prefetch={false} href={l.href} className="text-sm hover:underline">{l.label}</Link>)}
                        </nav>
                    )}
                    <div className="flex flex-col gap-2">
                        <h2 className="press border-b border-border pb-1 text-sm tracking-wide">{t('CONTACT')}</h2>
                        {store.email && <a href={`mailto:${store.email}`} className="text-sm hover:underline">{store.email}</a>}
                        {socials.length > 0 && (
                            <ul className="mt-2 flex gap-0" aria-label={t('FOLLOW_US')}>
                                {socials.map(s => {
                                    const Icon = SOCIAL_ICONS[s.provider.toLowerCase()];
                                    const url = s.url.startsWith('http') ? s.url : `https://${s.url}`;
                                    return (
                                        <li key={s.provider} className="-ms-px first:ms-0">
                                            <a href={url} target="_blank" rel="noopener noreferrer" aria-label={s.provider}
                                               className="flex size-9 items-center justify-center border border-foreground transition-colors duration-(--motion-fast) hover:bg-primary hover:text-primary-foreground">
                                                <Icon className="size-4"/>
                                            </a>
                                        </li>
                                    );
                                })}
                            </ul>
                        )}
                    </div>
                </div>
            </div>
            <div className="border-t border-border">
                <p className="press mx-auto max-w-content px-gutter py-3 text-xs tracking-wide text-muted-foreground">
                    © {new Date().getFullYear()} {store.name}. {t('RIGHT_RESERVED')}
                </p>
            </div>
        </footer>
    );
}
