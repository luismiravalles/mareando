package miravalles.tumareapro.data;

/**
 * Interefaz que asocia el nombre de un puerto con la clase f�sica que lo representa.
 * En cada a�o podr� haber una implementaci�n.
 * @author Luis
 *
 */
public interface MapeoPuertosAnual {

	// Debe retornar el nombre de la clase dado el nombre del puerto
	// por ejemplo para "Pasajes" puede retornar "Datos1"...
	String getNombreClase(String puerto);
}
