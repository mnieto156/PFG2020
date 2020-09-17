// tipos estructurados CONJUNTOS

program nueve;

type
       unoAdiez = set of 1..10;

var
       conj1 : unoAdiez;
	 conj2 : unoAdiez;
	 conj3 : unoAdiez;
begin
        write ("TIPOS ESTRUCTURADOS CONJUNTOS");
	
	conj1:=[2..4];
	conj2:=[3..7];
	conj3:=conj1+conj2;

end.
