package vigica.tools;

import org.springframework.stereotype.Component;

@Component
public class DVBS2Reader extends AbstractReader {

	private static final byte[] END_MAGIC = {(byte) 0x00, (byte) 0x00, (byte) 0x3F, (byte) 0xFF};

	@Override
	protected int getOffset(byte version) {
		return (version == 0x0E ? (4*16 + 2) : 0);
	}

	@Override
	protected byte[] getEndMagic() {
		return END_MAGIC;
	}
}
