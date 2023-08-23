package chokistream;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.swing.Icon;

public class LogoIcon implements Icon {
	private static final String sOutline = "wTSxQNEjorFAsROlsUOjsQWnsUmosQKhsVKnuZo7FWorl6OxYKO5WjsWSkuUo7FoprlKOxcqm5SjsYCkuXo7GHpLECqLGTo7mKaxk6G5ahsZSouUo7GXuSpbGZorkrIBqLkrIEpbkrIJo7kqmyFKG5Smshm5ajsiOpuYo7InpbEBqLIpqbEFo7Iyo7EJqbIzp7EWsjWxIqGyNqOxKLI2qbEzprI2qbFGobI2qbFWqbIzqLFlobInpLFzo7IhsXemshKjsXemsgKisXemsZKhsXKosYSjsWSlsXimsWemsXilsXCosXexcqixdKaxdKixcqKxdqGxaKmxdqGxZaexdqGxYKWxdKGxVaSxcKaxUKWxZ6GxRaaxY6OxQrFYobFCsVSmsUKxUqWxQ6OxUqGxQ6WxRKaxQaKxOKexQLE0sUD8E5o7FkqdFCprFkqbFGo7FmpbFQprFwpbFRpWZ7FxpLFSpTM7Fyo7FTpbFzorFJqLFyoDM7FGobFwqGZ7FCpLFpp7E5prFoqbE3qbFnqbE3obFno7E2o7Fmp7E2orFlqbE2p7FlpbE3obFlorE3qLFkqbE5o7FkqfwROisgGksRWksgKm0RipsgSjsSOlsgaxKaWyB6mxMqWyCKixNKWyCamxNaGyEKSxNaWyEKexNaayEbE1obIRo7E0orIRqbEyprISo7EwobISo7EnprISo7EkpbIRp7EhqLIQpLEZobIJobEXpbIHqLEWpbIGpOETorIBpPs=";
	private static final String cOutline = "wTmzOm0TKzOmsSWzWhsRiks3qbEJqLQaaxAqe0eiuXorVKS5Gnthprh6i3KhuHqLgqK4eouSo7kKS5mkuVo7EHo7EAorEVorEHpLEhprEWobElpbEkqLEppLExsTClsTimsTClsUaisTClsVKpsSipsVmmsSWnsWKxJKaxY6ixI6mxZKaxI6ixZ6exI6KxaaixIqixcqGxIbF0qLEYqLF2qbEVqbF4prESpbGAo7EJobGBorEFqLGBorECpLGBormbGApLlqmxeKe5SnsXe5KlsXSiuQqLFxo7kKixaKS5CosWaiuSpbFkorlKOxYqK5ahsWGhuWp7FYo7l6axVaW5ilsVKouZsVC5mxQqO5mxN6O5aisTOiuJpbEwobhKOxKKW3iosSiltyprEopbaKKxKaW2WjsTGhtjo7Eyp7YaOxNKW2CksTemtgpLFAp7YKSxRKG2GksUektjp7FIp7ZKaxSae2WmsVCltmprFRo7Z6axUqe2mosVSktzsVWot1p7FXpLd6mxWae3mlsWK4GhsWSnuBp7FnpbgaexcKO4GnsXOnuAqLF2o7eaGxgbdamxg6W3CjsYOltko7GDpbVqSxgaK0misXaltDprF0orQKixcKizipsWeis4qbFmorOKmxZaOzmxZKOzmisWOzmlsWKjs5prFhqbOaaxYaWzmmsWCjs5orFYorOKKxUrNaOxRaWzOosTmzOm8=";
	private static final String sInner = "wTSxRdEkobFFsRWmsUepsQinsVOnsQWmsVaisQOisVmisQGlsWKmuZqLFmuZsWmluZsXOhuZsYCksQKjsYapsQinsZKnsRGksZWxFKKxl7EXo7GYpbEgpLIAsSSpsgGnsTCnsgOlsTSyBKWxNqSyBaaxN6myBqixOaSyCLFAobIJpLFAobIRobFAobISqLE5orIUo7E3o7IVpbE1pLIWp7EzsheisSmpsheisSaosheisSKpshaksRmmshSpsRajshOksROpshGlsRKksgmisQmisgSjsQeisgGksQajsgClsQSpsZipsQOksZihsQGosZihsQCisZihuYqbGYqLl6myAKG5apsgGkuWp7ICprlqeyBKe5ansgipuXpbITormbIXprEApbIisQKksiSpsQSksiaisQeyJ6mxEaKyKaSxF7IwprEiqLIxqLEopLIypbEzprIypbFFpLIypbFUqbIpp7Fio7IksWmnshijsXOyEaOxc7ICprFzsZKnsWeosYWisVejsYChsVWxebFJpbF3obFBsXSmsTSnsXKosTGmsXChsTGmsWalsTGmsWSnsTKjsWOisTOmsWKhsTSpsWGxNqmxYKSxOaGxYKSxQ6mxYKSxSKexYqexU6axZ6OxWKWxcamxWaSxcqWxYKSxc6GxYaSxc6exYqixdKGxZKSxdKGxZrF0obFosXOjsWmksXGosXCosXCjsXGlsWijsXGlsWaxcaWxYqGxcLFXqbFmqbFTprFjqLFJo7FhsUehsViisUehsVelsUehsVaosUeisVahsUelsVShsUiisVKosUimsVKksUimsVKxSKaxUaOxSKWxUKSxSKKxQ6WxRqGxOLFFsTSxRQ";
	private static final String cInner = "wTmzij0TKms4pbEmpLOaaxIKO0KisRKktFp7EFqbUKaxAbV6K5ahtjqLkqW3O5KluCorkqW5GkuUqLl6a5mjsQSosQOosRKxEKKxF6exGKGxIaOxJrEkqbExqLElqbE4prElqbFFpLElqbFRpbEkpLFXprEhpbFgorEgo7Fio7EZpbFjqLEZorFmpbEYp7Foo7EYobFporEXo7FxorEVprFyqbETo7F0pLEQo7F1qbEHo7F2prEEp7F2prECo7F2prmamxdqG5iosXWhuXpbF0oblqGxcqW5WlsXGkuVpbFwo7laWxaaG5aisWemuXp7FmobmaKxY6OxAKmxYLECsVansQOhsVOlsQOnsVChsQOnsUGhsQOnsTShuZqLEpo7kamxJae4axI6m3mmsSOptypbEjqbZ6SxJaG2OjsSemtgorEwobV6GxM6W1WmsTentVprFBqbVaaxRqK1exUKK1mosVGotgqbFTorYqKxVKO2OnsVWktlorFWqLZ6SxWKa3CnsVmotzsWGht0prFipLdaWxY6e3aksWWkt2qbFnpbdqmxaaa3apsXGpt2orFzp7dbF3obcqexeKi2mhsXiotkobF4qLV7F2qLUaGxcqm0aksXGjtEpLFppLQ6SxZ6O0OksWamtDpLFlqbQ6WxZaW0OmsWOptEsWKntEorFhqbRKKxYaG0SisVm0OmsVaktCo7FQqLOaexRbOKSxObOKP7";
	private static final char[] hexToChar = new char[] {'0','1','2','3','4','5','6','7','8','9','.',' ','M','C','L','Z'};

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		Graphics2D g2 = (Graphics2D) g;
		
		g2.scale(1/3.0, 1/3.0);
		
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		g2.setColor(Color.decode("#d1b139"));
		g2.fill(new Ellipse2D.Double(0, 0, 270, 270));
		g2.setColor(Color.decode("#ed1c24"));
		g2.fill(new Ellipse2D.Double(12.5, 12.5, 245, 245));
		g2.setColor(Color.decode("#561f00"));
		g2.fill(new Ellipse2D.Double(25, 25, 220, 220));
		
		g2.setColor(Color.WHITE);
		parseCurves(g2, cOutline);
		parseCurves(g2, sOutline);
		g2.setColor(Color.decode("#ed1c24"));
		parseCurves(g2, sInner);
		parseCurves(g2, cInner);
		
		g2.scale(3, 3);
	}
	
	private static void parseCurves(Graphics2D g, String s) {
		Path2D p = new Path2D.Float(Path2D.WIND_EVEN_ODD);
		byte[] bytes = Base64.getDecoder().decode(s);
		char command = 'L';
		StringBuilder num = new StringBuilder(8);
		List<Double> args = new ArrayList<>();
		for(int i = 0; i < bytes.length*2; i++) {
			int c = i % 2 == 0 ? (bytes[i/2] & 0xf0) >> 4 : bytes[i/2] & 0x0f;
			// end number
			if(c > 10 && num.length() > 0) {
				args.add(Double.parseDouble(num.toString()));
				num = new StringBuilder(8);
				if(command == 'M' && args.size() == 2) {
					p.moveTo(args.get(0), args.get(1));
					args.clear();
				} else if(command == 'L' && args.size() == 2) {
					p.lineTo(args.get(0), args.get(1));
					args.clear();
				} else if(command == 'C' && args.size() == 6) {
					p.curveTo(args.get(0), args.get(1), args.get(2), args.get(3), args.get(4), args.get(5));
					args.clear();
				}
			}
			
			if(c > 11) {
				command = hexToChar[c]; // new command
				if(command == 'Z') p.closePath();
			} else if (c < 11) {
				num.append(hexToChar[c]); // part of number
			}
		}
		g.fill(p);
	}

	@Override
	public int getIconWidth() {
		return 90;
	}

	@Override
	public int getIconHeight() {
		return 90;
	}
}
