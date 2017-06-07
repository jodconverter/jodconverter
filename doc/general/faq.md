# Frequently Asked Questions

## Table of Contents

- [Do I really need to install OpenOffice.org in order to use JODConverter?](#do-i-really-need-to-install-openofficeorg-in-order-to-use-jodconverter)
- [Do I really really need to install OpenOffice.org? There's no way to install only a few libraries/DLLs instead?](#do-i-really-really-need-to-install-openofficeorg-theres-no-way-to-install-only-a-few-librariesdlls-instead)
- [How well does it convert format X to format Y?](#how-well-does-it-convert-format-x-to-format-y)
- [When converting from format X to Y something in the output is not quite right](#when-converting-from-format-x-to-y-something-in-the-output-is-not-quite-right)
- [So if it is an OpenOffice.org issue and not a JODConverter one where can I ask for more help?](#so-if-it-is-an-openofficeorg-issue-and-not-a-jodconverter-one-where-can-i-ask-for-more-help)
- [Is there an option to set X \(image quality, layout mode, hidden text, etc\)?](#is-there-an-option-to-set-x-image-quality-layout-mode-hidden-text-etc)
- [When converting to HTML using the webapp images are missing?](#when-converting-to-html-using-the-webapp-images-are-missing)
- [When will the next JODConverter version be released?](#when-will-the-next-jodconverter-version-be-released)


> Whenever OpenOffice.org (OOo for short) is mentioned, this can generally be interpreted to include any office suite derived from OOo such as [Apache OpenOffice](https://www.openoffice.org) and [LibreOffice](https://www.libreoffice.org).


### Do I really need to install OpenOffice.org in order to use JODConverter?

Yes. In fact, JODConverter simply automates OOo operations; all actual conversions are performed by OOo. Trying to use JODConverter without OOo would be like trying to use say the MySQL JDBC driver without a MySQL database server. If it can't connect to a server, the driver is useless.

### Do I really really need to install OpenOffice.org? There's no way to install only a few libraries/DLLs instead?

Yes you do really need a complete OOo installation. At most you can omit Base and a few optional component when installing. (Splitting OOo conversion filters into independent components is one of the goals of an OOo sub-project called ODF Toolkit, but don't hold your breath.)

### How well does it convert format X to format Y?

Different people have different requirements/expectations. Why don't you just find out for yourself? Since JODConverter simply automates OOo conversions, you don't even need to install JODConverter to do some tests, you just need OOo. Manually open a document in format X with OOo, and save it as (or export it to) format Y. Voilà! No tricks, no gimmicks.

### When converting from format X to Y something in the output is not quite right

As mentioned, JODConverter simply automates OOo conversions. 90% of the problems reported on the JODConverter group are actually OOo problems. Please try performing a manual conversion using OOo alone as explained in the previous answer. If you still get the same incorrect result, then it's clearly an OOo issue. (Note that the reverse is not true. If you get different results that doesn't mean it's clearly a JODConverter issue: it may still be an OOo issue that affects only its headless mode.)

### So if it is an OpenOffice.org issue and not a JODConverter one where can I ask for more help?

The OpenOffice.org Forums is probably the best place to start. If you are sure it is a bug then you can report it as such using the OOo issue tracker.

### Is there an option to set X (image quality, layout mode, hidden text, etc)?

Guess what? It depends on OOo. Start up Writer, Calc, or Impress, save/export a document in the format you want to convert it to, and see which options OOo provides. Once you've found out which options you want to set you can ask on the group how to automate the same operation.

### When converting to HTML using the webapp images are missing

HTML is a bit special. Converting to most other formats results in a single output file, but when converting to HTML OOo generates multiple files: one HTML file plus various image files (OOo puts them in the same directory as the HTML one). The sample webapp has no special support for HTML output (in fact its form page doesn't list HTML as an option), it just does what works for other formats i.e. returns a single file: the HTML one. Hence images are lost. The webapp does not even attempt to provide a solution for this problem, because the exact solution depends on your particular requirements. In some cases you may want to package HTML and images into a ZIP file in order to return a single file. In other cases you may want to copy HTML and images to a public path on your web server to access them directly. In all cases you should think about security implications. It's up to you.

### When will the next JODConverter version be released?

The project is maintained entirely on a voluntary basis in the developer's free time, so unfortunately there are no scheduled release dates.
