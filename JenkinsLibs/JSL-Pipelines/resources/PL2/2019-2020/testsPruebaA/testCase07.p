// tipos estructurados CONJUNTOS operaciones

program nueve;

type
       unoAdiez = set of 1..10;
	 dosAcinco = set of 2..5;

var
       conj1 : unoAdiez;
	 conj2 : unoAdiez;
	 conj3 : unoAdiez;
begin
        write ("CONJUNTOS OPERACIONES");
	  writeln();
	conj1:=[2..4];
	conj2:=[3..7];
	conj3:=conj1+conj2;
	
	write ("IN(true):");
	if ((2 IN conj3) and (6 IN conj3)) then
		write("true");
	else
		write("false");
	
end.
