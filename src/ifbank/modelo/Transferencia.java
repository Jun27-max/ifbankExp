package ifbank.modelo;

public class Transferencia {
	private int contaOrigem;
	private int contaDestino;
	private double valor;

	public int getContaOrigem() {
		return contaOrigem;
	}

	public void setContaOrigem(int contaOrigem) {
		this.contaOrigem = contaOrigem;
	}

	public int getContaDestino() {
		return contaDestino;
	}

	public void setContaDestino(int contaDestino) {
		this.contaDestino = contaDestino;
	}

	public double getValor() {
		return valor;
	}

	public void setValor(double valor) {
		this.valor = valor;
	}
}
