package skillable.jscs;

import java.beans.PropertyEditorSupport;


public class SignedSessionEditor extends PropertyEditorSupport {

	private ObjectSigner signer;

	public void setKey(String key) {
		this.signer = new ObjectSigner(key);
	}

	@Override
	public String getAsText() {
		if (getValue() == null)
			return null;
		return ((SignedSession) getValue()).toString();
	}

	@Override
	public void setAsText(String text) {
		setValue(SignedSession.fromString(text, signer));
	}

}