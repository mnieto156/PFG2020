// subprogramas (paso por referencia) procedimientos

program once;



var
        z: integer;


procedure incrementa (var x:integer);

  
        begin
            x:= x+1;            
        end;

begin

      write ("SUBPROGRAMAS PROCEDIMIENTOS");
      writeln();

      z:=1;
      incrementa (z);
	write("z(2):");
      write(z);

end.
