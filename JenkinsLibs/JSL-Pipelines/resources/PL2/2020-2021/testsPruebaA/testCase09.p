// subprogramas (paso por referencia) funciones

program doce;



var
        z,v: integer;


function incrementa (var x:integer):integer;

var
        y:integer;

        begin
		x:=x+1;
            y:=x;
            incrementa:=y;

        end;

begin

        write ("SUBPROGRAMAS FUNCIONES");
        writeln();

        z:=1;
        v:= incrementa (z);

        write("v(2):");
        write(v);
	
	  write("z(2):");
        write(z);

end.
