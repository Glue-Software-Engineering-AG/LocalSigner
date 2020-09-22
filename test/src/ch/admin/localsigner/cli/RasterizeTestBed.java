/*
 * Copyright 2020 The Federal Authorities of the Swiss Confederation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package ch.admin.localsigner.cli;

import java.io.IOException;
import org.apache.log4j.Logger;
import org.junit.Ignore;

/**
 * Testbed for benchmarking conversion from PDF to rasterized PDF.
 *
 *
 * Will test four options: 1. Render to hi-res B&W, then re-render to low-res
 * color/gray if required 2. Render to low-res Color, then convert to gray or
 * re-render as hi-res B&W if required 3. Render to hi-res Color then downsample
 * as required 4. No change - render at 200dpi color.
 *
 * While rendering one letter per page will be printed to System.out: C, G or B
 * for Color, Grayscale or B&W.
 */

@Ignore
public class RasterizeTestBed {

  private static final Logger LOGGER = Logger.getLogger(RasterizeTestBed.class);

  public static void main(String[] args) throws IOException {

    /*
     * PDF pdf = new PDF(new PDFReader(new File("D:\\tmp\\t1.pdf"))); // PDF
     * rasterpdf; if (pdf.getNumberOfPages() > 50) { // Limit testing to 50
     * pages. pdf.getPages().subList(50, pdf.getPages().size()).clear(); }
     *
     * int colordpi = 200; int bwdpi = 300;
     *
     * doItParallel(pdf, colordpi, bwdpi);
     *
     *
     * // long t1 = System.currentTimeMillis(); // rasterpdf = rasterize(pdf,
     * bwdpi, colordpi, ByRerenderToColor); // rasterpdf.render(new
     * FileOutputStream("D:\\tmp\\t2_1.pdf")); // // long t2 =
     * System.currentTimeMillis(); // rasterpdf = rasterize(pdf, bwdpi,
     * colordpi, ByRerenderTo1Bit); // rasterpdf.render(new
     * FileOutputStream("D:\\tmp\\t2_2.pdf")); // // long t3 =
     * System.currentTimeMillis(); // rasterpdf = rasterize(pdf, bwdpi,
     * colordpi, ByDownsampling); // rasterpdf.render(new
     * FileOutputStream("D:\\tmp\\t1_3.pdf")); // // long t4 =
     * System.currentTimeMillis(); // rasterpdf = rasterize(pdf, bwdpi,
     * colordpi, NoChange); // rasterpdf.render(new
     * FileOutputStream("D:\\tmp\\t2_4.pdf")); // // long t5 =
     * System.currentTimeMillis(); //
     * LOGGER.info("Rasterize took "+(t2-t1)+" "+(t3-t2)+" "+(t4-t3)+" //
     * "+(t5-t4)); }
     *
     * @SuppressWarnings("unused") private static void doItParallel(PDF pdf, int
     * colordpi, int bwdpi) { Callable<String> runnableTask1 = () -> { final PDF
     * rasterpdf; long t1 = System.currentTimeMillis(); rasterpdf =
     * rasterize(pdf, bwdpi, colordpi, ByRerenderToColor); long t2 =
     * System.currentTimeMillis(); try { rasterpdf.render(new
     * FileOutputStream("D:\\tmp\\t1_1.pdf"));
     * LOGGER.info("Rasterize to t1_1.pdf took "+(t2-t1)); return "r1"; } catch
     * (IOException e) { e.printStackTrace(); return "r1"; }
     *
     * };
     *
     * Callable<String> runnableTask2 = () -> { final PDF rasterpdf; long t1 =
     * System.currentTimeMillis(); rasterpdf = rasterize(pdf, bwdpi, colordpi,
     * ByRerenderTo1Bit); long t2 = System.currentTimeMillis(); try {
     * rasterpdf.render(new FileOutputStream("D:\\tmp\\t1_2.pdf"));
     * LOGGER.info("Rasterize to t1_2.pdf took "+(t2-t1)); return "r2"; } catch
     * (IOException e) { e.printStackTrace(); return "r2"; }
     *
     * };
     *
     * Callable<String> runnableTask3 = () -> { final PDF rasterpdf; long t1 =
     * System.currentTimeMillis(); rasterpdf = rasterize(pdf, bwdpi, colordpi,
     * NoChange); long t2 = System.currentTimeMillis(); try {
     * rasterpdf.render(new FileOutputStream("D:\\tmp\\t1_3.pdf"));
     * LOGGER.info("Rasterize to t1_3.pdf took "+(t2-t1)); return "r3"; } catch
     * (IOException e) { e.printStackTrace(); return "r3"; }
     *
     * };
     *
     * Callable<String> runnableTask4 = () -> { long t11 =
     * System.currentTimeMillis(); PDF rasterpdf = rasterize(pdf, bwdpi,
     * colordpi, ByDownsampling); long t21 = System.currentTimeMillis(); try {
     * rasterpdf.render(new FileOutputStream("D:\\tmp\\t1_4.pdf"));
     * LOGGER.info("Rasterize to t1_4.pdf took " + (t21 - t11)); return "r4"; }
     * catch (IOException e) { e.printStackTrace(); return "r4"; }
     *
     * };
     *
     * List<Callable<String>> callables = new ArrayList<>(); //
     * callables.add(runnableTask1); callables.add(runnableTask2); //
     * callables.add(runnableTask3); // callables.add(runnableTask4);
     *
     * int procs = Runtime.getRuntime().availableProcessors(); ExecutorService
     * executor = Executors.newFixedThreadPool(procs); try {
     * List<Future<String>> futures = executor.invokeAll(callables); // for
     * (Future<String> f : futures) // { //
     * System.out.format("%s - %s -- %d %n", f.get(), f.isDone(), //
     * System.currentTimeMillis()); // } executor.shutdown(); } catch
     * (InterruptedException e) { e.printStackTrace(); } }
     *
     * public static PDF rasterize(PDF pdf, int bwdpi, int colordpi, Rasterizer
     * rasterizer) {
     *
     * ICC_Profile icc = ICC_Profile.getInstance(ColorSpace.CS_sRGB);
     * OutputProfile profile = new OutputProfile(OutputProfile.PDFA1b_2005,
     * "sRGB", null, "http://www.color.org", null, icc); PDF newpdf = new
     * PDF(profile); PDFParser parser = new PDFParser(pdf); List<PDFPage> pages
     * = pdf.getPages(); for (int i=0;i<pages.size();i++) {
     *
     * parser.setOutputProfile(profile); PDFPage page = pdf.getPage(i);
     * PagePainter painter = parser.getPagePainter(page); List<BufferedImage>
     * bufimage = rasterizer.toBitmap(painter, profile, colordpi, bwdpi);
     *
     * for (BufferedImage b : bufimage) { PDFImage pdfimage = null; try {
     * pdfimage = new PDFImage(b); } catch (InterruptedException e) { // Can't
     * happen with BufferedImage } PDFPage newpage =
     * newpdf.newPage(page.getWidth(), page.getHeight());
     * newpage.drawImage(pdfimage, 0, 0, page.getWidth(), page.getHeight()); } }
     * System.out.println(); return newpdf; }
     *
     */
    /**
     * Given a 24-bit RGB image, optionally resize it and/or reduce the number
     * of bits per pixel to 8 or 1
     *
     * @param image
     *          the image to be resize - must be 24-bit RGB with byte-based
     *          Raster (DataBufferByte)
     * @param indpi
     *          the DPI the image is currently in
     * @param outdpi
     *          the DPI to convert the image to
     * @param bpp
     *          the number of bits per pixel - 24 for RGB, 8 for Grayscale or 1
     *          for B&amp;W
     * @return the modified image
     */
    // private static BufferedImage fixImage2(BufferedImage image, int indpi,
    // int outdpi,
    // int bpp)
    // {
    // // Assuming a DataBufferByte for both input and output image - this will
    // // always be
    // // the case in this eaxmple, but some ColorModels (eg
    // // ColorModel.getRGBdefault) use
    // // a DataBufferInt. So this is not a general purpose routine, but as used
    // // here this
    // // assumption makes the code a lot simpler and more efficient.
    // ColorModel cm = image.getColorModel();
    // WritableRaster raster = image.getRaster();
    // int w = raster.getWidth();
    // int h = raster.getHeight();
    //
    // if (indpi != outdpi)
    // {
    // double scale = (double) outdpi / indpi;
    // w *= scale;
    // h *= scale;
    // WritableRaster outraster = cm.createCompatibleWritableRaster(w, h);
    // AffineTransform tran = AffineTransform.getScaleInstance(scale, scale);
    // // This is typically hardware accelerated (or at least native code)
    // // so can't be beat for performance.
    // AffineTransformOp scaler = new AffineTransformOp(tran,
    // AffineTransformOp.TYPE_BILINEAR);
    // scaler.filter(raster, outraster);
    // raster = outraster;
    // }
    //
    // if (bpp == 8)
    // {
    // // Remove color information but keep 1 pixel per byte - easy.
    // cm = PDFParser.GRAYSCALE;
    // WritableRaster outraster = cm.createCompatibleWritableRaster(w, h);
    // byte[] in = ((DataBufferByte) raster.getDataBuffer()).getData();
    // byte[] out = ((DataBufferByte) outraster.getDataBuffer()).getData();
    // int i = 0, j = 0;
    // while (i < in.length)
    // {
    // int rgb = ((in[i++] & 0xFF) << 16) | ((in[i++] & 0xFF) << 8) | (in[i++] &
    // 0xFF);
    // // This is fast way of converting RGB to Grayscale. It's an integer
    // // based version of the standard PAL/NTSC grayscale formula:
    // // gray = 0.3red + 0.59green + 0.11blue
    // int gray = rgb == 0xFFFFFF ? 255
    // : ((((rgb & 0xFF0000) / 850) + (((rgb << 8) & 0xFF0000) / 432)
    // + ((rgb << 16) & 0xFF0000) / 2318)) >> 8;
    // out[j++] = (byte) gray;
    // }
    // raster = outraster;
    // }
    // else if (bpp == 1)
    // {
    // // Remove color information and reduce to 1 bit per pixel, or 7 pixels
    // per
    // // byte.
    // // Still fairly simple, we just need to byte align each row.
    // cm = PDFParser.BLACKANDWHITE;
    // WritableRaster outraster = cm.createCompatibleWritableRaster(w, h);
    // byte[] in = ((DataBufferByte) raster.getDataBuffer()).getData();
    // byte[] out = ((DataBufferByte) outraster.getDataBuffer()).getData();
    // int i = 0, j = 0;
    // for (int y = 0; y < h; y++)
    // {
    // int x = 0, n = 0;
    // for (x = 0; x < w; x++)
    // {
    // int rgb = ((in[i++] & 0xFF) << 16) | ((in[i++] & 0xFF) << 8) | (in[i++] &
    // 0xFF);
    // int gray = rgb == 0xFFFFFF ? 255
    // : ((((rgb & 0xFF0000) / 850) + (((rgb << 8) & 0xFF0000) / 432)
    // + ((rgb << 16) & 0xFF0000) / 2318)) >> 8;
    // n <<= 1;
    // if (gray > 128)
    // { // 128 is normal threshold, but you can adjust
    // n |= 1;
    // }
    // if ((x & 7) == 7)
    // { // Finished 8 pixel block - push it to output
    // out[j++] = (byte) n;
    // n = 0;
    // }
    // }
    // if ((x & 7) != 7)
    // { // Image isn't multiple of 8 wide - shift and push it to output
    // n <<= 8 - (x & 7);
    // out[j++] = (byte) n;
    // }
    // }
    // raster = outraster;
    // }
    // else if (bpp != 24)
    // {
    // throw new IllegalArgumentException("bpp must be 24, 8 or 1");
    // }
    // if (raster != image.getRaster())
    // {
    // image = new BufferedImage(cm, raster, false, null);
    // }
    // return image;
    // }
    //
    // //---------------------------------------------------------------------------
    //
    // @FunctionalInterface
    // interface Rasterizer {
    // List<BufferedImage> toBitmap(PagePainter painter, OutputProfile profile,
    // int colordpi,
    // int bwdpi);
    // }
    //
    // static final Rasterizer ByDownsampling = (painter, profile, colordpi,
    // bwdpi) -> {
    // List<BufferedImage> imgLst = new ArrayList<>();
    // BufferedImage image = painter.getImage(bwdpi, PDFParser.RGB);
    // if (profile.isSet(OutputProfile.Feature.ColorImage)
    // || profile.isSet(OutputProfile.Feature.ColorContent))
    // {
    // System.out.print("C");
    // // image = fixImage(image, bwdpi, colordpi, 24);
    // imgLst.add(fixImage2(image, bwdpi, colordpi, 24));
    // }
    // else if (profile.isSet(OutputProfile.Feature.GrayscaleImage)
    // || profile.isSet(OutputProfile.Feature.GrayscaleContent))
    // {
    // System.out.print("G");
    // // image = fixImage(image, bwdpi, colordpi, 8);
    // imgLst.add(fixImage2(image, bwdpi, colordpi, 8));
    // }
    // else
    // {
    // System.out.print("B");
    //
    // // image = fixImage(image, bwdpi, bwdpi, 1);
    // imgLst.add(fixImage2(image, bwdpi, bwdpi, 1));
    // }
    // return imgLst;// image;
    // };
    //
    // static final Rasterizer ByRerenderTo1Bit = (painter, profile, colordpi,
    // bwdpi) -> {
    // List<BufferedImage> imgLst = new ArrayList<>();
    // BufferedImage image = painter.getImage(colordpi, PDFParser.RGB);
    // if (profile.isSet(OutputProfile.Feature.ColorImage)
    // || profile.isSet(OutputProfile.Feature.ColorContent))
    // {
    // System.out.print("C");
    // }
    // else if (profile.isSet(OutputProfile.Feature.GrayscaleImage)
    // || profile.isSet(OutputProfile.Feature.GrayscaleContent))
    // {
    // System.out.print("G");
    // // image = fixImage(image, colordpi, colordpi, 8);
    // imgLst.add(fixImage2(image, colordpi, colordpi, 8));
    //
    // }
    // else
    // {
    // System.out.print("B");
    // // image = painter.getImage(bwdpi, PDFParser.BLACKANDWHITE);
    // imgLst.add(painter.getImage(bwdpi, PDFParser.BLACKANDWHITE));
    // // }
    // // return imgLst;// image;
    // }
    // return imgLst;
    // };
    //
    // static final Rasterizer ByRerenderToColor = (painter, profile, colordpi,
    // bwdpi) -> {
    // List<BufferedImage> imgLst = new ArrayList<>();
    // painter.getImage(bwdpi, PDFParser.BLACKANDWHITE);
    // // if (profile.isSet(OutputProfile.Feature.ColorImage)
    // // || profile.isSet(OutputProfile.Feature.ColorContent))
    // // {
    // System.out.print("C");
    // // image = painter.getImage(colordpi, PDFParser.RGB);
    // imgLst.add(painter.getImage(colordpi, PDFParser.RGB));
    // // }
    // // else if (profile.isSet(OutputProfile.Feature.GrayscaleImage)
    // // || profile.isSet(OutputProfile.Feature.GrayscaleContent))
    // // {
    // System.out.print("G");
    // // image = painter.getImage(colordpi, PDFParser.GRAYSCALE);
    // imgLst.add(painter.getImage(colordpi, PDFParser.GRAYSCALE));
    // // }
    // // else
    // // {
    // System.out.print("B");
    // // }
    // return imgLst;// image;
    // };
    //
    // static final Rasterizer NoChange = (painter, profile, colordpi, bwdpi) ->
    // {
    // List<BufferedImage> imgLst = new ArrayList<>();
    // imgLst.add(painter.getImage(colordpi, PDFParser.RGB));
    // return imgLst;
    // };
  }
}
