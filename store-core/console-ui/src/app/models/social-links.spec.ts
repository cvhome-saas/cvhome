import {expectedSocialHost, socialLinkIcon, socialLinkProblem} from './social-links';

/**
 * The pairing of a provider with its URL.
 *
 * This check moved here from store management's reactive form when social links became the content
 * service's, and it kept no test on the way — the old coverage was a DOM assertion on a settings
 * section that no longer exists. It matters more than it looks: nothing server-side validates the
 * pairing, so a TikTok URL saved in the Facebook row renders on the storefront under a Facebook mark.
 */
describe('social links', () => {
  it('accepts a profile on the provider\'s own site, with or without a scheme', () => {
    expect(socialLinkProblem('FACEBOOK', 'https://facebook.com/acme')).toBeNull();
    expect(socialLinkProblem('FACEBOOK', 'facebook.com/acme')).toBeNull();
    expect(socialLinkProblem('INSTAGRAM', 'https://www.instagram.com/acme')).toBeNull();
  });

  it('accepts the alternates these companies still serve', () => {
    expect(socialLinkProblem('X', 'https://twitter.com/acme')).toBeNull();
    expect(socialLinkProblem('FACEBOOK', 'https://fb.com/acme')).toBeNull();
  });

  it('accepts a subdomain of the provider, which is why the check is a suffix match', () => {
    expect(socialLinkProblem('FACEBOOK', 'https://m.facebook.com/acme')).toBeNull();
  });

  it('rejects a link to a different provider than the row it is in', () => {
    expect(socialLinkProblem('FACEBOOK', 'https://tiktok.com/@acme')).toBe('host');
  });

  it('rejects the site itself, which is not a profile on it', () => {
    expect(socialLinkProblem('FACEBOOK', 'https://facebook.com')).toBe('profile');
    expect(socialLinkProblem('FACEBOOK', 'https://facebook.com/?ref=1')).toBe('profile');
  });

  it('rejects something that is not a web address at all', () => {
    expect(socialLinkProblem('FACEBOOK', 'not a url')).toBe('url');
  });

  it('treats empty as nothing to check — a blank row is how a link is removed', () => {
    expect(socialLinkProblem('FACEBOOK', '')).toBeNull();
    expect(socialLinkProblem('FACEBOOK', '   ')).toBeNull();
  });

  /*
   * The provider list is the server's: `GET /public/social-links-providers` can name a member the
   * console has not been taught. Such a row still has to save, so everything degrades rather than
   * throwing — it keeps the shape check and loses the host check, which there is no list for.
   */
  it('lets a provider the console does not know keep a field that saves', () => {
    expect(socialLinkProblem('PINTEREST', 'https://pinterest.com/acme')).toBeNull();
    expect(socialLinkProblem('PINTEREST', 'https://example.com/acme')).toBeNull();
    expect(socialLinkProblem('PINTEREST', 'not a url')).toBe('url');
    expect(socialLinkIcon('PINTEREST')).toBe('link');
    expect(expectedSocialHost('PINTEREST')).toBeNull();
  });

  it('names one host in the message, because two would be a quiz', () => {
    expect(expectedSocialHost('X')).toBe('x.com');
    expect(socialLinkIcon('X')).toBe('xSocial');
  });
});
