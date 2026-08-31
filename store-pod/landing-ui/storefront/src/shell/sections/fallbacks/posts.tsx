import Image from 'next/image';
import {Link} from '@store-front/i18n/navigation';
import type {SectionRenderProps} from '@store-front/theme';
import {EmptyOrHint, SectionHeading} from './shared';

function Posts({section, data, preview}: SectionRenderProps) {
    const posts = data?.posts?.content ?? [];
    if (posts.length === 0) {
        return <EmptyOrHint preview={preview} label="Blog posts — nothing published yet"/>;
    }
    return (
        <div>
            <SectionHeading title={section.text.title}/>
            <div className="grid gap-6 md:grid-cols-3">
                {posts.map(post => (
                    <Link key={post.id} href={`/blog/${post.slug}`} className="group block">
                        {post.heroImageUrl && (
                            <div className="relative mb-3 aspect-[3/2] overflow-hidden rounded-md bg-muted">
                                <Image src={post.heroImageUrl} alt={post.title} fill className="object-cover transition-transform duration-300 group-hover:scale-105"/>
                            </div>
                        )}
                        <h3 className="font-medium group-hover:underline"><bdi dir="auto">{post.title}</bdi></h3>
                        {post.excerpt && (
                            <p className="mt-1 line-clamp-2 text-sm text-muted-foreground">
                                <bdi dir="auto">{post.excerpt}</bdi>
                            </p>
                        )}
                    </Link>
                ))}
            </div>
        </div>
    );
}

export const postsFallback = {cards: Posts};
