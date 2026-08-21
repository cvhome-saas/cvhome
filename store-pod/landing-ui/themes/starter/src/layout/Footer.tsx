import {getTranslations} from 'next-intl/server';
import {FacebookIcon, GithubIcon, InstagramIcon, LinkedinIcon, TwitterIcon, YoutubeIcon, type LucideIcon} from 'lucide-react';
import {Link} from '@store-front/i18n/navigation';
import type {LayoutData, PageContext} from '@store-front/theme';

const SOCIAL_ICONS: Record<string, LucideIcon> = {
    facebook: FacebookIcon, twitter: TwitterIcon, x: TwitterIcon, instagram: InstagramIcon, github: GithubIcon,
    youtube: YoutubeIcon, linkedin: LinkedinIcon,
};

export async function Footer({data}: { ctx: PageContext; data: LayoutData }) {
    const t = await getTranslations('COMPONENTS.FOOTER');
    const {store} = data;
    const shop = data.categories.filter(c => c.description).slice(0, 8);
    const info = data.pages.filter(p => p.description);
    const socials = (store.socialLinks ?? []).filter(s => SOCIAL_ICONS[s.provider.toLowerCase()]);
    return (
        <footer className="mt-section border-t bg-muted/40">
            <div className="mx-auto grid max-w-content gap-10 px-gutter py-12 sm:grid-cols-2 lg:grid-cols-4">
                <div className="flex flex-col gap-3">
                    <p className="text-lg font-semibold">{store.name}</p>
                    {(store.address?.address || store.address?.city) && (
                        <address className="text-sm not-italic text-muted-foreground">
                            {[store.address.address, store.address.city, store.address.postalCode, store.address.country].filter(Boolean).join(', ')}
                        </address>
                    )}
                </div>
                {shop.length > 0 && (
                    <nav aria-label={t('SHOP')} className="flex flex-col gap-2">
                        <h2 className="text-sm font-semibold uppercase tracking-wide text-muted-foreground">{t('SHOP')}</h2>
                        {shop.map(c => <Link key={c.code} prefetch={false} href={`/category/${c.description.friendlyUrl}`} className="text-sm hover:underline">{c.description.name}</Link>)}
                    </nav>
                )}
                {info.length > 0 && (
                    <nav aria-label={t('INFORMATION')} className="flex flex-col gap-2">
                        <h2 className="text-sm font-semibold uppercase tracking-wide text-muted-foreground">{t('INFORMATION')}</h2>
                        {info.map(p => <Link key={p.code} prefetch={false} href={`/content/${p.description.friendlyUrl}`} className="text-sm hover:underline">{p.description.name}</Link>)}
                    </nav>
                )}
                <div className="flex flex-col gap-2">
                    <h2 className="text-sm font-semibold uppercase tracking-wide text-muted-foreground">{t('CONTACT')}</h2>
                    {store.email && <a href={`mailto:${store.email}`} className="text-sm hover:underline">{store.email}</a>}
                    {store.phone && <a href={`tel:${store.phone}`} className="text-sm hover:underline" dir="ltr">{store.phone}</a>}
                    {socials.length > 0 && (
                        <ul className="mt-2 flex gap-2" aria-label={t('FOLLOW_US')}>
                            {socials.map(s => {
                                const Icon = SOCIAL_ICONS[s.provider.toLowerCase()];
                                const url = s.url.startsWith('http') ? s.url : `https://${s.url}`;
                                return (
                                    <li key={s.provider}>
                                        <a href={url} target="_blank" rel="noopener noreferrer" aria-label={s.provider}
                                           className="flex size-9 items-center justify-center rounded-control border hover:bg-muted">
                                            <Icon className="size-4"/>
                                        </a>
                                    </li>
                                );
                            })}
                        </ul>
                    )}
                </div>
            </div>
            <div className="border-t">
                <p className="mx-auto max-w-content px-gutter py-4 text-xs text-muted-foreground">
                    © {new Date().getFullYear()} {store.name}. {t('RIGHT_RESERVED')}
                </p>
            </div>
        </footer>
    );
}
