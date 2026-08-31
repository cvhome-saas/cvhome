import {Link} from '@store-front/i18n/navigation';
import type {SectionRenderProps} from '@store-front/theme';
import {EmptyOrHint, SectionHeading} from './shared';

function Categories({section, data, preview}: SectionRenderProps) {
    const categories = data?.categories ?? [];
    if (categories.length === 0) {
        return <EmptyOrHint preview={preview} label="Categories — none to show yet"/>;
    }
    const pills = section.variant === 'pills';
    return (
        <div>
            <SectionHeading title={section.text.title}/>
            {pills ? (
                <div className="flex flex-wrap gap-2">
                    {categories.map(c => (
                        <Link key={c.id} href={`/category/${c.description?.friendlyUrl ?? c.code}`}
                              className="rounded-full border px-4 py-2 text-sm transition-colors hover:bg-muted">
                            <bdi dir="auto">{c.description?.name ?? c.code}</bdi>
                        </Link>
                    ))}
                </div>
            ) : (
                <div className="grid grid-cols-2 gap-4 md:grid-cols-3">
                    {categories.map(c => (
                        <Link key={c.id} href={`/category/${c.description?.friendlyUrl ?? c.code}`}
                              className="flex min-h-24 flex-col justify-end rounded-md bg-muted p-4 transition-colors hover:bg-muted/70">
                            <span className="font-medium"><bdi dir="auto">{c.description?.name ?? c.code}</bdi></span>
                            {c.productCount > 0 && (
                                <span className="text-xs text-muted-foreground">{c.productCount}</span>
                            )}
                        </Link>
                    ))}
                </div>
            )}
        </div>
    );
}

export const categoriesFallback = {grid: Categories, pills: Categories};
