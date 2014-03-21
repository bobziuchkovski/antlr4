package org.antlr.v4.runtime;

import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.misc.Pair;

/**
 * Provides an implementation of {@link TokenSource} as a wrapper
 * around an existing {@link TokenStream} object.  Tokens emitted by
 * this wrapper source are cloned to prevent changes to the originals.
 */
public class StreamSource implements TokenSource {
	protected final TokenStream input;
	protected final int start;
	protected final int stop;
	protected int index;

	protected TokenFactory<?> factory;

	StreamSource(TokenStream input, Interval viewInterval) {
		this.input = input;

		// Validate interval bounds and adjust as needed
		int start = viewInterval.a;
		int stop = viewInterval.b;
		if (start < 0) start = 0;
		if (stop > input.size() - 1) stop = input.size() - 1;
		this.start = start;
		this.stop = stop;

		this.index = this.start;
		this.factory = null;
	}

	/** Clone an input Token.  Our stream view clones tokens so that the original tokens are
	 * not modified.  This is particularly important to preserve the source Token's index.
	 * */
	protected Token cloneToken(Token t) {
		if (factory != null) {
			return factory.create(new Pair<TokenSource, CharStream>(this, null), t.getType(), t.getText(), t.getChannel(),
					-1, -1, getLine(), getCharPositionInLine());
		} else {
			return new CommonToken(t);
		}
	}

	protected Token createEOF() {
		// All tokens exhausted.  Return EOF
		if (factory != null) {
			return factory.create(new Pair<TokenSource, CharStream>(this, null), Token.EOF, "",
					Token.DEFAULT_CHANNEL, -1, -1, getLine(), getCharPositionInLine());
		} else {
			return new CommonToken(new Pair<TokenSource, CharStream>(this, null), Token.EOF, Token.DEFAULT_CHANNEL,
					-1, -1);
		}
	}

	@Override
	public Token nextToken() {
		if (index <= stop && index < input.size()) {
			Token t = input.get(index);
			index++;
			return cloneToken(t);
		} else {
			return createEOF();
		}
	}

	@Override
	public int getLine() {
		// Possible TODO: boolean switch to enable line tracking
		return -1;
	}

	@Override
	public int getCharPositionInLine() {
		// Possible TODO: boolean switch to enable column tracking
		return -1;
	}

	@Override
	public CharStream getInputStream() {
		return null;
	}

	@Override
	public String getSourceName() {
		return input.getSourceName() + "-view";
	}

	@Override
	public void setTokenFactory(@NotNull TokenFactory<?> factory) {
		this.factory = factory;
	}

	@Override
	public TokenFactory<?> getTokenFactory() {
		return factory;
	}
}
